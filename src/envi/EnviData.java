package envi;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import envi.EnviHeader.DataType;
import envi.EnviHeader.Interleave;

public class EnviData {
	private final static int OUTPUT_FIELDS = 4;

	private String filename;
	private EnviHeader header;
	private int[] requestedBands;
	private FileInputStream  fileIn;
	private DataInputStream in;	
	private int lines, samples, pixels, bytesPerBand;

	public EnviData(String filename, EnviHeader header) {
		this.filename = filename;
		this.header = header;		
	}

	public void close() {
		try {
			in.close();
			fileIn.close();
		} catch (IOException e) {
			System.out.println("IO exception while reading \""+ filename +"\": "+e.getMessage());
			System.exit(0);
		}
	}

	public BufferedImage getImage(int red, int green, int blue) {	
		//Check input		
		if (red < header.bands && green < header.bands && blue < header.bands) {
			requestedBands = new int[3];			
			this.requestedBands[0] = red;
			this.requestedBands[1] = green;
			this.requestedBands[2] = blue;
		} else {
			System.out.println("Selected band not present in input file");
			System.exit(0);
		}
		
		//File
		try {
			if (filename.endsWith(".img") || filename.endsWith(".IMG")) {
				File inFile = new File(filename);
				fileIn  = new FileInputStream(inFile);
				in = new DataInputStream(fileIn);
			} else {
				throw new UnsupportedFormatException("cannot open file: \""
						+ filename + "\", unsupported file format");
			}

		} catch (FileNotFoundException e) {
			System.out.println("File \""+ filename +"\" not found");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IO exception while reading \""+ filename +"\": "+e.getMessage());
			System.exit(0);
		}

		//Setup
		BufferedImage image = new BufferedImage(header.samples, header.lines, BufferedImage.TYPE_INT_ARGB);
		lines = header.lines;
		samples = header.samples;
		pixels = lines*samples;
		bytesPerBand = header.bpp*pixels;

		ByteBuffer[] bandBuffs = new ByteBuffer[3]; 
		bandBuffs[0] = ByteBuffer.allocate(bytesPerBand);
		bandBuffs[0].clear();
		bandBuffs[1] = ByteBuffer.allocate(bytesPerBand);
		bandBuffs[1].clear();
		bandBuffs[2] = ByteBuffer.allocate(bytesPerBand);
		bandBuffs[2].clear();

		//Read input file into buffer
		ByteBuffer inBuf  = ByteBuffer.allocate(bytesPerBand*header.bands);
		inBuf.clear();
		try {
			in.readFully(inBuf.array(), 0, bytesPerBand*header.bands);
			inBuf.rewind();
		} catch (IOException e) {
			System.out.println("IO Exception while trying to read input file");
			System.exit(0);
		}

		//Fill the band buffers
		if (header.interleave == Interleave.BSQ) {
			readBSQ(inBuf, bandBuffs);
		} else if (header.interleave == Interleave.BIP) {
			readBIP(inBuf, bandBuffs);
		} else if (header.interleave == Interleave.BIL) {
			readBIL(inBuf, bandBuffs);
		}
				
		//Switch the byte order if warranted
		if (header.byteOrder == ByteOrder.BIG_ENDIAN) {
			bandBuffs[0].order(ByteOrder.BIG_ENDIAN);
			bandBuffs[1].order(ByteOrder.BIG_ENDIAN);
			bandBuffs[2].order(ByteOrder.BIG_ENDIAN);
		} else {
			bandBuffs[0].order(ByteOrder.LITTLE_ENDIAN);
			bandBuffs[1].order(ByteOrder.LITTLE_ENDIAN);
			bandBuffs[2].order(ByteOrder.LITTLE_ENDIAN);
		}

		//Fill output buffer	
		ByteBuffer outBuf = ByteBuffer.allocate(pixels*OUTPUT_FIELDS);
		outBuf.clear();
		if ( header.datatype == DataType.byte8 ) {
			while (outBuf.hasRemaining()) {
				outBuf.put((byte) 0xFF);
				outBuf.put(bandBuffs[0].get());
				outBuf.put(bandBuffs[1].get());
				outBuf.put(bandBuffs[2].get());
			}
		} else if (header.datatype == DataType.short16) {
			while (outBuf.hasRemaining()) {
				outBuf.put((byte) 0xFF);
				outBuf.put((byte) ((bandBuffs[0].getShort()/Short.MAX_VALUE)*255));
				outBuf.put((byte) ((bandBuffs[1].getShort()/Short.MAX_VALUE)*255));
				outBuf.put((byte) ((bandBuffs[2].getShort()/Short.MAX_VALUE)*255));
			}
		} else if (header.datatype == DataType.int32) {
			while (outBuf.hasRemaining()) {				
				outBuf.put((byte) 0xFF);
				outBuf.put((byte) ((bandBuffs[0].getInt()/Integer.MAX_VALUE)*255));
				outBuf.put((byte) ((bandBuffs[1].getInt()/Integer.MAX_VALUE)*255));
				outBuf.put((byte) ((bandBuffs[2].getInt()/Integer.MAX_VALUE)*255));
			}
		} else if (header.datatype == DataType.float32) {		
			while (outBuf.hasRemaining()) {				
				outBuf.put((byte) 0xFF);
				outBuf.put((byte) ((bandBuffs[0].getFloat())*255));
				outBuf.put((byte) ((bandBuffs[1].getFloat())*255));
				outBuf.put((byte) ((bandBuffs[2].getFloat())*255));
			}
		} else if (header.datatype == DataType.double64) {
			while (outBuf.hasRemaining()) {				
				outBuf.put((byte) 0xFF);
				outBuf.put((byte) ((bandBuffs[0].getDouble())*255));
				outBuf.put((byte) ((bandBuffs[1].getDouble())*255));
				outBuf.put((byte) ((bandBuffs[2].getDouble())*255));
			}
		} else {
			System.out.println("Datatype not yet implemented");
			System.exit(0);
		}

		outBuf.rewind();

		int [] tmp = new int[pixels];
		outBuf.asIntBuffer().get(tmp);
		image.setRGB(0, 0, samples, lines, tmp, 0, samples);

		return image;
	}
	
	//methods for the different interleave formats

	private void readBSQ(ByteBuffer inBuf, ByteBuffer[] bandBuffs) {
		int[] offsets = new int[3];
		offsets[0] = bytesPerBand*requestedBands[0];
		offsets[1] = bytesPerBand*requestedBands[1];
		offsets[2] = bytesPerBand*requestedBands[2];

		for (int i=0; i< 3; i++) {
			inBuf.position(offsets[i]);
			inBuf.get(bandBuffs[i].array(), 0, bytesPerBand);
			inBuf.rewind();
		}
		bandBuffs[0].rewind();
		bandBuffs[1].rewind();
		bandBuffs[2].rewind();
	}

	private void readBIP(ByteBuffer inBuf, ByteBuffer[] bandBuffs) {
		ByteBuffer pixelBuf = ByteBuffer.allocate(header.bpp);
		pixelBuf.clear();		

		for (int i=0; i<pixels; i++) {
			for (int band=0; band<header.bands; band++) {
				inBuf.get(pixelBuf.array(), 0, header.bpp);
				pixelBuf.rewind();
				if (band == requestedBands[0]) {
					bandBuffs[0].put(pixelBuf);
				}
				pixelBuf.rewind();
				if (band == requestedBands[1]) {
					bandBuffs[1].put(pixelBuf);
				}
				pixelBuf.rewind();
				if (band == requestedBands[2]) {
					bandBuffs[2].put(pixelBuf);
				}
				pixelBuf.rewind();
			}			
		}

		bandBuffs[0].rewind();
		bandBuffs[1].rewind();
		bandBuffs[2].rewind();
	}

	private void readBIL(ByteBuffer inBuf, ByteBuffer[] bandBuffs) {
		int bytesPerLine = samples*header.bpp;

		ByteBuffer lineBuffer = ByteBuffer.allocate(bytesPerLine*header.bands);
		lineBuffer.clear();

		ByteBuffer lineBandBuffer = ByteBuffer.allocate(bytesPerLine);
		lineBandBuffer.clear();

		for (int i=0; i<header.lines; i++) {			
			inBuf.get(lineBuffer.array(), 0, bytesPerLine*header.bands);
			lineBuffer.rewind();

			for (int band=0; band<header.bands; band++) {
				lineBuffer.get(lineBandBuffer.array(), 0, bytesPerLine);
				lineBandBuffer.rewind();

				if (band == requestedBands[0]) {
					bandBuffs[0].put(lineBandBuffer);
				} 
				if (band == requestedBands[1]) {
					bandBuffs[1].put(lineBandBuffer);
				}
				if (band == requestedBands[2]) {
					bandBuffs[2].put(lineBandBuffer);
				}
			}			
		}
		bandBuffs[0].rewind();
		bandBuffs[1].rewind();
		bandBuffs[2].rewind();
	}
}
