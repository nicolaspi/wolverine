package com.nicolaspi.wolverine.test;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.Properties;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.nicolaspi.wolverine.Wolverine;

public class OpenCVTest {
	
	@Test
	public void loadOpenCV()
	{
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
	}
}
