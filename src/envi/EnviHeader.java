package envi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteOrder;

public class EnviHeader {
	public enum DataType { byte8, short16, int32, float32, double64, complex2x32, complex2x64, uint16, ulong32, long64, ulong64 };
	public enum Interleave { BSQ, BIP, BIL }; //bsq = band sequential, bip = band interleaved by pixel, bil = band interleaved by line
	
	public String description;
	public int samples;
	public int lines;
	public int bands;
	public int offset;
	public String filetype;
	public DataType datatype;
	public Interleave interleave;
	public String sensortype;
	public ByteOrder byteOrder;
	public String mapinfo;
	public String wavelengthunits;
	public String[] bandnames;	
	
	public int bpp;

	public EnviHeader(String filename) throws UnsupportedFormatException {
		try {
			if (filename.endsWith(".hdr") || filename.endsWith(".HDR")) {
				File file = new File(filename);

				Reader r 			= new FileReader(file);			
				BufferedReader br 	= new BufferedReader(r);

				String line = br.readLine();
				if (!line.equals("ENVI")) { 
					throw new UnsupportedFormatException("cannot open file: \""
							+ filename + "\", unsupported file format, first line should be \"ENVI\"");
				};
				while((line = br.readLine()) != null) {
					try {
						if (line.startsWith("description")) {
							//discard this line and read the next lines for the description information
							String temp = "";
							while((line = br.readLine()) != null && !line.endsWith("}")) {
								temp += line.trim();
							}
							//remove the trailing '}'
							String last = line.substring(0, line.length()-1);
							description = temp+last;
							
						} else if (line.startsWith("samples")) {
							String[] split = line.split("=");
							samples = Integer.parseInt(split[1].trim());

						} else if (line.startsWith("lines")) {
							String[] split = line.split("=");
							lines = Integer.parseInt(split[1].trim());

						} else if (line.startsWith("bands")) {
							String[] split = line.split("=");
							bands = Integer.parseInt(split[1].trim());

						} else if (line.startsWith("header offset")) {
							String[] split = line.split("=");
							offset = Integer.parseInt(split[1].trim());

						} else if (line.startsWith("file type")) {
							String[] split = line.split("=");
							filetype = split[1].trim();

						} else if (line.startsWith("data type")) {
							String[] split = line.split("=");
							int type = Integer.parseInt(split[1].trim());
							switch (type) {
								case 1  : datatype = DataType.byte8; 		bpp =   1; break;
								case 2  : datatype = DataType.short16; 		bpp =   2; break;
								case 3  : datatype = DataType.int32; 		bpp =   4; break;
								case 4  : datatype = DataType.float32; 		bpp =   4; break;
								case 5  : datatype = DataType.double64; 	bpp =   8; break;
								case 6  : datatype = DataType.complex2x32; 	bpp =   8; break;
								case 9  : datatype = DataType.complex2x64; 	bpp =  16; break;
								case 12 : datatype = DataType.uint16; 		bpp =   2; break;
								case 13 : datatype = DataType.ulong32; 		bpp =   4; break;
								case 14 : datatype = DataType.long64; 		bpp =   8; break;
								case 15 : datatype = DataType.ulong64; 		bpp =   8; break;
	
								default: throw new UnsupportedFormatException("Unknown data type: \""+type+"\" see ENVI file format for details.");
							}

						} else if (line.startsWith("interleave")) {
							String[] split = line.split("=");
							if (split[1].trim().equalsIgnoreCase("bsq")) {
								interleave = Interleave.BSQ;
							} else if (split[1].trim().equalsIgnoreCase("bip")) {
								interleave = Interleave.BIP;
							} else if (split[1].trim().equalsIgnoreCase("bil")) {
								interleave = Interleave.BIL;
							} else {
								throw new UnsupportedFormatException("Unknown interleave format: \""+split[1]+"\" see ENVI file format for details.");
							}
							
						} else if (line.startsWith("sensor type")) {
							String[] split = line.split("=");
							sensortype = split[1].trim();
							
						} else if (line.startsWith("byte order")) {
							String[] split = line.split("=");
							int type = Integer.parseInt(split[1].trim());
							switch (type) {
								case 0  : byteOrder = ByteOrder.LITTLE_ENDIAN; break;
								case 1  : byteOrder = ByteOrder.BIG_ENDIAN; break;
								
								default: throw new UnsupportedFormatException("Unknown byte order: \""+type+"\" see ENVI file format for details.");
							}
							
						} else if (line.startsWith("map info")) {
							String[] split = line.split("=");
							String temp = split[1].trim();
							//remove the ''{' and '}'
							mapinfo = temp.substring(1, temp.length()-1);							
						
						} else if (line.startsWith("wavelength units")) {
							String[] split = line.split("=");
							wavelengthunits = split[1].trim();
							
						} else if (line.startsWith("band names")) {
							//discard this line and read the next lines for the description information
							String temp = "";
							while((line = br.readLine()) != null && !line.endsWith("}")) {
								temp += line.trim();
							}
							//remove the trailing '}'
							String last = line.substring(0, line.length()-1);
							temp += last;
							
							bandnames = temp.split(",");
							
						} 
					} catch (NumberFormatException e) {
						throw new UnsupportedFormatException("Unknown line: \""+line+"\" see ENVI file format for details.");
					}
				}


				br.close();
				r.close();
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
	}
}
