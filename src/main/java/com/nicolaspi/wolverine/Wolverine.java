package com.nicolaspi.wolverine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;;


public class Wolverine
{
	static short spritesOffset = 2;
	static int maximumSpaces = 500000;
	static boolean initialized = false;
	static int maxHeight = 0;
	static int maxWidth = 0;
	static int maxWidthSquare = 0;
	static int maxWidthSquareMaxHeight = 0;
   static private void init() {
	   if(!initialized) {
		   System.load( "/usr/local/share/OpenCV/java/libopencv_java"+Core.VERSION_EPOCH+Core.VERSION_MAJOR+Core.VERSION_MINOR+".so" );
		   initialized = true;
	   }
   }
   /**
    * Takes a BufferedImage, splits it in sprite components and pack them in a smaller image.
    * Returns a List<SpriteInfo> containing information on sprites components.
    *
    * @param  fileName file name where the packed sprites image will be written.
    * @param  bimg a BufferedImage in abgr format containing sprite components separated by black-transparent pixels of value zero. 
    * @return A List<SpriteInfo> containing sprites infos.
    */
   static public List<SpriteInfo> spritePng( String fileName, BufferedImage bimg)
   {
	  init();
	  List<SpriteInfo> sprites = new LinkedList<SpriteInfo>();
      Mat img = null;
      Mat alphaImg = null;
      
      
      if(bimg != null) {
    	  int[] data = ((DataBufferInt) bimg.getRaster().getDataBuffer()).getData();
    	  int[] datacpy = new int[data.length];
    	  ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
    	  byte[] alpha = new byte[data.length]; 
          IntBuffer intBuffer = byteBuffer.asIntBuffer();
          int abgr;
          for(int i =0;i<data.length; ++i){
        	  abgr = data[i];
        	  
        	  if(abgr != 0) {
        		  alpha[i] = (byte)0xFF;//(byte)(abgr >>> 24);
        		  datacpy[i] = (abgr << 8) | (abgr >>> 24);
        		 
        	  }
          }
          intBuffer.put(datacpy);
          byte[] pixels = byteBuffer.array();

	      img = new Mat(bimg.getHeight(),bimg.getWidth(),CvType.CV_8UC4);
	      img.put(0, 0, pixels);
	      alphaImg = new Mat(bimg.getHeight(),bimg.getWidth(),CvType.CV_8UC1);
	      alphaImg.put(0, 0, alpha);
	      //Highgui.imwrite(path, alphaImg);
      } else {
    	 img = Highgui.imread(fileName,-1);
      }
      int type = img.type();
      Mat gray = new Mat();
      Mat hierarchy = new Mat();
      Imgproc.cvtColor(img, img, Imgproc.COLOR_BGRA2RGBA);
      List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
      if(alphaImg == null) {
	      Imgproc.threshold(img, gray, 0, 1, Imgproc.THRESH_BINARY);
	      //Imgproc.Canny( img, gray, thresh, thresh*2);
	      //Imgproc.cvtColor(gray, gray, Imgproc.COLOR_RGB2GRAY);
	      Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
      } else {
    	  Imgproc.findContours(alphaImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
      }
      List<Mat> spritesCropped = new ArrayList<Mat>();
      maxHeight = 0;
      for(MatOfPoint cont : contours){
    	  Rect br = Imgproc.boundingRect(cont);
    	  spritesCropped.add(new Mat(img, br ));
    	  maxHeight += br.height+spritesOffset;
      }
      Collections.sort( spritesCropped, new 
    		  Comparator< Mat >( ){
	    	  		public int compare(Mat a, Mat b){
	    	  			return (b.width() - a.width());
	    	  		}
      });
      Size wSize = new Size();
      boolean createNewSpace = true;
      Point ofs = new Point();
	  if(spritesCropped.isEmpty()) {
		  return sprites;
	  }
	  short minW = (short)spritesCropped.get(spritesCropped.size()-1).width();
	  maxWidth = spritesCropped.get(0).width();
	  maxWidthSquare = maxWidth*maxWidth;
	  maxWidthSquareMaxHeight = maxWidthSquare*maxHeight;
	  int height = 0;
	  SortedSet<Rectangle> spaces = new TreeSet<Rectangle>(new Comparator<Rectangle>(){
          public int compare(Rectangle a, Rectangle b){
              return maxWidthSquareMaxHeight*a.dims[1] + (maxHeight - a.dims[3])*maxWidthSquare + (maxWidth-a.dims[2])*maxWidth + a.dims[0] - (maxWidthSquareMaxHeight*b.dims[1] + (maxHeight - b.dims[3])*maxWidthSquare + (maxWidth-b.dims[2])*maxWidth + b.dims[0]);
          }
      }
  );
	  spaces.add(new Rectangle((short)0,(short)0,(short)maxWidth,(short)maxHeight));
	  Mat nimg = new Mat(maxHeight,maxWidth,type,new Scalar(0.0,0.0,0.0,0.0));
	  int processed = 0;
      for(Mat mat : spritesCropped) {
    	  mat.locateROI(wSize, ofs);
    	  //Search for an empty place
    	  List<Rectangle> subRects = new LinkedList<Rectangle>();
    	 if(spaces.size() > maximumSpaces) {
    		  createNewSpace = false;
//    		  System.out.println("Many spaces, reduced service mode");
    	  }
    	  boolean foundPlace = false;
    	  try {
	    	  for(Rectangle r : spaces){
	    		  if(r.canContain((short)mat.width(), (short)mat.height())) {
	    			  foundPlace = true;
	    			  //compute y position (top or bottom ?)
	    			  int left = r.getX();
	    			  int right = r.getW()+r.getX()-mat.width();
	    			  int posx;
	    			  //which is least in the middle ?
	    			  if( Math.abs(left+mat.width()/2 - maxWidth/2) > Math.abs(right+mat.width()/2 - maxWidth/2)){
	    				  posx = left;
	    			  } else {
	    				  posx = right;
	    			  }
	    			  Rectangle rect = new Rectangle((short)posx,(short)r.getY(),(short)mat.width(),(short)mat.height());
	    			  sprites.add(new SpriteInfo(rect,(float)ofs.x,(float)ofs.y));
	    			  Mat tmp = new Mat(nimg, new Rect(rect.getX(),rect.getY(),rect.getW(),rect.getH()));
	    			  mat.copyTo(tmp);
	    			  int bottom = r.getY() + mat.height();
	    			  if(bottom>height) {
	    				  height = bottom;
	    			  }
	    			  //update rects
	    			  Iterator<Rectangle> it = spaces.iterator();
	    			  while(it.hasNext()){
	    				  if(it.next().subRectanglesUpdate(rect, subRects,minW,createNewSpace,spritesOffset)) {
	    					  it.remove();
	    				  }
	    			  }
	    			  if(!subRects.isEmpty()) {
	    				  spaces.addAll(subRects);
	    			  }
	    			  processed++;
	    			  break;
	    		  }
	    	  }
	    	  if(!foundPlace) {
					throw new Exception("Wolverine : No space found for sprite !");
	    	  }
    	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
//    	  System.out.println("processed" + processed + " on " + spritesCropped.size());
      }
      Rect br = new Rect(0,0,maxWidth,height);
      Mat newImage = new Mat(nimg, br);
      Highgui.imwrite(fileName, newImage);
      return sprites;
   }
}
