package com.nicolaspi.wolverine;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

public class Rectangle {
	short[] dims;
	protected void difference(Rectangle r) {
		short x = dims[0];
		short y = dims[1];
		short X = (short)(x + dims[2]);
		short Y = (short)(y + dims[3]);
		short rx = r.dims[0];
		short ry = r.dims[1];
		short rX = (short)(rx + r.dims[2]);
		short rY = (short)(ry + r.dims[3]);
		//compute difference (minimum reduction of one of the dim to exclude r)
		short dim = -1;
		short min = Short.MAX_VALUE;
		boolean left = false;
		short value = (short)(X-rx);
		if(value < min) {
			min = value;
			dim = 0;
			left = false;
		}
		value = (short)(rX-x);
		if(value < min) {
			min = value;
			dim = 0;
			left = true;
		}
		value = (short)(Y-ry);
		if(value < min) {
			min = value;
			dim = 1;
			left = false;
		}
		value = (short)(rY - y);
		if(value < min) {
			min = value;
			dim = 1;
			left = true;
		}
		if(value<0){ //rectangle is outside
			return;
		}
		
		if(left) {
			dims[0+dim] += value; 
		} else {
			dims[2+dim] -= value;
		}
	}
	protected boolean subRectanglesUpdate(Rectangle r, List<Rectangle> rectangles, short minW, boolean createNewSpace, short offset){
		short x = dims[0];
		short y = dims[1];
		short X = (short)(x + dims[2]);
		short Y = (short)(y + dims[3]);
		short rx = (short) (r.dims[0]);
		short ry = (short) (r.dims[1]);
		short rX = (short)(rx + r.dims[2]+offset);
		short rY = (short)(ry + r.dims[3]+offset);
		boolean updated = false;
		List<Rectangle> subs = new LinkedList<Rectangle>();
		
		if(rx > X ||
		        rX < x ||
		        ry > Y ||
		        rY < y) {
			//It is outside
			return false;
		}
			//left
			if(rx - x >= minW) {
				updated=true;
				this.update(x,y,(short)(rx-x),(short)(Y-y));
				if(!createNewSpace) {
					return false;
				}
			}
			//right
			if(X - rX >= minW) {
				if(updated) {
					subs.add(new Rectangle(rX,y,(short)(X-rX),(short)(Y-y)));
				} else {
					updated=true;
					this.update(rX,y,(short)(X-rX),(short)(Y-y));
					if(!createNewSpace) {
						return false;
					}
				}	
			}
			//bottom
			if(Y > rY) {
				subs.add(new Rectangle(x,rY,(short)(X-x),(short)(Y-rY)));
			}
			//top //Should be never reached
			/*if(ry > y) {
				if(updated) {
					subs.add(new Rectangle(x,y,(short)(X-x),(short)(ry-y)));
				} else {
					updated=true;
					this.update(x,y,(short)(X-x),(short)(ry-y));
					if(!createNewSpace) {
						return false;
					}
				}
			}*/
		if(!subs.isEmpty()) {
			rectangles.addAll(subs);
		}
		if(updated == true){
			//System.out.println("Rectangles size : " + rectangles.size() + " subs : " + subs.size());
			return false;
		}else {
			//System.out.println("Removed");
			return true;
		}	
	}
	protected boolean contains(Rectangle r){
		short x = dims[0];
		short y = dims[1];
		short X = (short)(x + dims[2]);
		short Y = (short)(y + dims[3]);
		short rx = r.dims[0];
		short ry = r.dims[1];
		short rX = (short)(rx + r.dims[2]);
		short rY = (short)(ry + r.dims[3]);
		return X >= rX && x <= rx && Y >= rY && y <= rY;
	}
	protected boolean canContain(Rectangle r){
		return dims[2] >= r.dims[2] && dims[3] >= r.dims[3];
	}
	protected boolean canContain(short w, short h){
		return dims[2] >= w && dims[3] >= h;
	}
	
	protected Rectangle(short x, short y, short w, short h) {
		this.dims = new short[4];
		this.dims[0] = x;
		this.dims[1] = y;
		this.dims[2] = w;
		this.dims[3] = h;
	}
	protected short getX() {
		return dims[0];
	}
	protected short getY() {
		return dims[1];
	}
	protected short getW() {
		return dims[2];
	}
	protected short getH() {
		return dims[3];
	}
	protected void update(Rectangle r) {
		this.dims[0] = r.dims[0];
		this.dims[1] = r.dims[1];
		this.dims[2] = r.dims[2];
		this.dims[3] = r.dims[3];
	}
	protected void update(short x, short y, short w, short h) {
		this.dims[0] = x;
		this.dims[1] = y;
		this.dims[2] = w;
		this.dims[3] = h;
	}
	
}
