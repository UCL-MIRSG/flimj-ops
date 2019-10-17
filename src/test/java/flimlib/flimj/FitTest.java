/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2017 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package flimlib.flimj;

import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;

import io.scif.img.ImgOpener;
import io.scif.lifesci.SDTFormat;
import io.scif.lifesci.SDTFormat.Reader;
import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.convert.RealTypeConverter;
import flimlib.flimj.FitParams;
import flimlib.flimj.FitResults;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.RealMask;
import net.imglib2.roi.geom.real.OpenWritableBox;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

/**
 * Regression tests for {@link RealTypeConverter} ops.
 * 
 * @author Dasong Gao
 */
public class FitTest extends AbstractOpTest {

	static RandomAccessibleInterval<UnsignedShortType> in;

	static FitParams<UnsignedShortType> param_master;
	static FitParams<UnsignedShortType> param;

	static long[] min, max, vMin, vMax;

	static RealMask roi;

	private static final long SEED = 0x1226;

	private static final Random rng = new Random(SEED);

	private static final int NSAMPLE = 5;

	private static final float TOLERANCE = 1e-5f;

	@BeforeClass
	@SuppressWarnings("unchecked")
	public static void init() throws IOException {
		Reader r = new SDTFormat.Reader();
		r.setContext(new Context());
		r.setSource("test_files/input.sdt");
		in = (Img<UnsignedShortType>) new ImgOpener().openImgs(r).get(0).getImg();

		// input and output boundaries
		min = new long[] { 0, 40, 40 };
		max = new long[] { 63, 87, 87 };
		vMin = min.clone();
		vMax = max.clone();

		in = Views.hyperSlice(in, 3, 12);
		r.close();

		param_master = new FitParams<UnsignedShortType>();
		param_master.ltAxis = 0;
		param_master.xInc = 0.195f;
		param_master.transMap = in;
		param_master.fitStart = 9;
		param_master.fitEnd = 20;
		param_master.paramFree = new boolean[] { true, true, true };
		param_master.dropBad = false;

		// +/- 1 because those dimensions are the closure of the box
		roi = new OpenWritableBox(new double[] { min[1] - 1, min[2] - 1 }, new double[] { max[1] + 1, max[2] + 1 });
	}

	@Before
	public void initParam() {
		param = param_master.copy();
	}

	// @Test
	public void testVisual() throws IOException {
		RealMask roi = new OpenWritableBox(new double[] { 49 - 1, 18 - 1 }, new double[] { 57 + 1, 24 + 1 });
		roi = new OpenWritableBox(new double[] { 55 - 1, 24 - 1 }, new double[] { 57 + 1, 24 + 1 });
		roi = null;
		Reader r = new SDTFormat.Reader();
		// io.scif.formats.ICSFormat.Reader r = new io.scif.formats.ICSFormat.Reader();
		r.setContext(new Context());
		r.setSource("test_files/test2.sdt");
		// r.setSource(".../Csarseven.ics");
		param = new FitParams<UnsignedShortType>();
		param.ltAxis = 0;
		param.xInc = 10.006715f / 256;
		param.transMap = (Img<UnsignedShortType>) new ImgOpener().openImgs(r).get(0).getImg();
		// param.xInc = 12.5f / 64;
		// param.transMap = in;
		// param.getChisqMap = true;
		// param.getResidualsMap = true;
		// param.paramFree = new boolean[] { false };
		// param.param = new float[] { 1.450f, 9.233f, 1.054f, 3.078f, 0.7027f };
		// param.dropBad = true;
		// param.iThresh = 90f;
		param.iThreshPercent = 10;
		param.nComp = 2;
		param.chisq_target = 0;


		
		long ms = System.currentTimeMillis();
		// param.dropBad = false;
		Img<DoubleType> knl = FlimOps.SQUARE_KERNEL_3;
		// knl = null;
		// param.fitStart = 40;
		// param.param = new float[] { 0f, 25f, 0f };
		// param.paramMap = ((FitResults) ops.run("flim.fitRLD", param, roi, knl)).paramMap;
		// param.fitStart = 41;
		// param.dropBad = true;
		FitResults out = (FitResults) ops.run("flim.fitMLA", param, roi, knl);
		System.out.println("Finished in " + (System.currentTimeMillis() - ms) + " ms");
		// System.out.println(ops.stats().min((IterableInterval)out.retCodeMap));
		// Demo.showResults(out.paramMap);
		// ImageJFunctions.show(out.residualsMap);
		
		// input
		// ImageJFunctions.show( (RandomAccessibleInterval<ARGBType>)  ops.run("flim.showPseudocolor", out, 0.2f, 2.5f) );
		
		// test2
		// ImageJFunctions.show( (RandomAccessibleInterval<ARGBType>)  ops.run("flim.showPseudocolor", out, 0.821f, 1.184f) );
		// ImageJFunctions.show( (RandomAccessibleInterval<ARGBType>)  ops.run("flim.showPseudocolor", out, 0.6f, 1.35f) );
		// ImageJFunctions.show( (RandomAccessibleInterval<ARGBType>)  ops.run("flim.showPseudocolor", out, 0.6f, 1.5f) );
		ImageJFunctions.show( (RandomAccessibleInterval<ARGBType>)  ops.run("flim.showPseudocolor", out, 0.0178, 2.695f) );
		
		// // test
		// // ImageJFunctions.show( (RandomAccessibleInterval<ARGBType>)  ops.run("flim.showPseudocolor", out, 0.5f, 2.323f) );
		while (true) {
			Demo.sleep20s();
		}
	}

	@Test
	public void testRLDFitImg() {
		long ms = System.currentTimeMillis();
		FitResults out = (FitResults) ops.run("flim.fitRLD", param);
		System.out.println("RLD finished in " + (System.currentTimeMillis() - ms) + " ms");

		float[] exp = { 2.5887516f, 1.3008053f, 0.1802666f, 4.498526f, 0.20362994f };
		assertSampleEquals(out.paramMap, exp);
	}
	
	@Test
	public void testBinning() {
		long ms = System.currentTimeMillis();
		FitResults out = (FitResults) ops.run("flim.fitRLD", param, roi, FlimOps.SQUARE_KERNEL_3);
		System.out.println("RLD with binning finished in " + (System.currentTimeMillis() - ms) + " ms");
		
		float[] exp = { 15.917448f, 34.33285f, 0.17224349f, 53.912094f, 0.19115955f };
		assertSampleEquals(out.paramMap, exp);
	}
	
	@Test
	public void testMLAFitImg() {
		// estimation using RLD
		param.paramMap = param.paramMap = ((FitResults) ops.run("flim.fitRLD", param, roi)).paramMap;
		
		long ms = System.currentTimeMillis();
		FitResults out = (FitResults) ops.run("flim.fitMLA", param, roi);
		System.out.println("MLA finished in " + (System.currentTimeMillis() - ms) + " ms");
		
		float[] exp = { 2.963042f, 2.1738043f, 0.15078613f, 5.6381326f, 0.18440692f };
		assertSampleEquals(out.paramMap, exp);
	}

	@Test
	public void testInstr() {
		// estimation using RLD
		param.paramMap = param.paramMap = ((FitResults) ops.run("flim.fitRLD", param, roi)).paramMap;

		// a trivial IRF
		param.instr = new float[12];
		param.instr[0] = 1;
		
		long ms = System.currentTimeMillis();
		FitResults out = (FitResults) ops.run("flim.fitMLA", param, roi);
		System.out.println("MLA with instr finished in " + (System.currentTimeMillis() - ms) + " ms");
		
		float[] exp = { 2.963042f, 2.1738043f, 0.15078613f, 5.6381326f, 0.18440692f };
		assertSampleEquals(out.paramMap, exp);
	}

	@Test
	public void testPhasorFitImg() {
		long ms = System.currentTimeMillis();
		FitResults out = (FitResults) ops.run("flim.fitPhasor", param, roi);
		System.out.println("Phasor finished in " + (System.currentTimeMillis() - ms) + " ms");

		float[] exp = { 0, 0.17804292f, 0.41997245f, 0.18927118f, 0.39349627f };
		assertSampleEquals(out.paramMap, exp);
	}

	@Test
	public void testGlobalFitImg() {
		long ms = System.currentTimeMillis();
		FitResults out = (FitResults) ops.run("flim.fitGlobal", param, roi);
		System.out.println("Global fit finished in " + (System.currentTimeMillis() - ms) + " ms");

		float[] exp = { 1.2399514f, 1.3008053f, 0.16449152f, 4.498526f, 0.16449152f };
		assertSampleEquals(out.paramMap, exp);
	}

	@Test
	public void testGlobalFitImgMultiExp() {
		param.nComp = 2;
		param.paramFree = new boolean[] { true, true, true, true, true };
		long ms = System.currentTimeMillis();
		FitResults out = (FitResults) ops.run("flim.fitGlobal", param, roi);
		System.out.println("Global fit (Multi) finished in " + (System.currentTimeMillis() - ms) + " ms");

		float[] exp = { 301.6971f, 0.1503315f, 430.5284f, 0.17790353f, 0.1503315f };
		assertSampleEquals(out.paramMap, exp);
	}

	private static <T extends RealType<T>> float[] getRandPos(IterableInterval<T> ii, int n, long...seed) {
		float[] arr = new float[n];
		rng.setSeed(seed.length == 0 ? SEED : seed[0]);
		int sz = (int) ii.size();
		Cursor<T> cursor = ii.cursor();
		long cur = 0;
		for (int i = 0; i < n; i++) {
			long next = rng.nextInt(sz);
			cursor.jumpFwd(next - cur);
			cur = next;
			arr[i] = cursor.get().getRealFloat();
		}
		return arr;
	}
	
	private static <T extends RealType<T>> void assertSampleEquals(RandomAccessibleInterval<T> map, float[] exp) {
		vMax[0] = map.max(param_master.ltAxis);
		float[] act = getRandPos(Views.interval(map, vMin, vMax), NSAMPLE);
		Assert.assertArrayEquals(exp, act, TOLERANCE);
	}
}
