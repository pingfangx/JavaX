package com.pingfangx.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MyByte {
	public static String str2HexStr(String str) {
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();

		for (int i = 0; i < bs.length; i++) {
			int bit = (bs[i] & 0xF0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0xF;
			sb.append(chars[bit]);
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];

		for (int i = 0; i < bytes.length; i++) {
			int n = str.indexOf(hexs[(2 * i)]) * 16;
			n += str.indexOf(hexs[(2 * i + 1)]);
			bytes[i] = ((byte) (n & 0xFF));
		}
		return new String(bytes);
	}

	public static String strToUnicode(String strText) throws Exception {
		StringBuilder str = new StringBuilder();

		for (int i = 0; i < strText.length(); i++) {
			char c = strText.charAt(i);
			int intAsc = c;
			String strHex = Integer.toHexString(intAsc);
			if (intAsc > 128) {
				str.append("\\u" + strHex);
			} else
				str.append("\\u00" + strHex);
		}
		return str.toString();
	}

	public static String unicodeToString(String hex) {
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = hex.substring(i * 6, (i + 1) * 6);

			String s1 = s.substring(2, 4) + "00";

			String s2 = s.substring(4);

			int n = Integer.valueOf(s1, 16).intValue() + Integer.valueOf(s2, 16).intValue();

			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}

	public static byte[] getMergeBytes(byte[] pByteA, byte[] pByteB) {
		int aCount = pByteA.length;
		int bCount = pByteB.length;
		byte[] b = new byte[aCount + bCount];
		for (int i = 0; i < aCount; i++) {
			b[i] = pByteA[i];
		}
		for (int i = 0; i < bCount; i++) {
			b[(aCount + i)] = pByteB[i];
		}
		return b;
	}

	public static byte[] cutOutByte(byte[] b, int j) {
		if ((b.length == 0) || (j == 0)) {
			return null;
		}
		byte[] tmp = new byte[j];
		for (int i = 0; i < j; i++) {
			tmp[i] = b[i];
		}
		return tmp;
	}

	public static byte[] cutOutByte(byte[] b, int j, int k) {
		if ((b.length == 0) || (k == 0) || (j == b.length)) {
			return null;
		}
		if (j + k > b.length) {
			k = b.length - j;
		}
		byte[] tmp = new byte[k];
		for (int i = 0; ((i < k ? 1 : 0) & (i + j < b.length ? 1 : 0)) != 0; i++) {
			tmp[i] = b[(i + j)];
		}
		return tmp;
	}

	public static byte[] HexString2Bytes(String hexstr) {
		byte[] b = new byte[hexstr.length() / 2];
		int j = 0;
		for (int i = 0; i < b.length; i++) {
			char c0 = hexstr.charAt(j++);
			char c1 = hexstr.charAt(j++);
			b[i] = ((byte) (parse(c0) << 4 | parse(c1)));
		}
		return b;
	}

	private static int parse(char c) {
		if (c >= 'a')
			return c - 'a' + 10 & 0xF;
		if (c >= 'A')
			return c - 'A' + 10 & 0xF;
		return c - '0' & 0xF;
	}

	public static String byteToHexString(byte b) {
		String stmp = Integer.toHexString(b & 0xFF);
		stmp = stmp.length() == 1 ? "0" + stmp : stmp;
		return stmp.toUpperCase();
	}

	public static int byteToInt(byte b) {
		return b & 0xFF;
	}

	public static String bytesToHexString(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		byte[] arrayOfByte = b;
		int j = b.length;
		for (int i = 0; i < j; i++) {
			byte c = arrayOfByte[i];
			stmp = Integer.toHexString(c & 0xFF);
			sb.append(stmp.length() == 1 ? "0" + stmp : stmp);
			sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}

	public static String BytesToHexString(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		byte[] arrayOfByte = b;
		int j = b.length;
		for (int i = 0; i < j; i++) {
			byte c = arrayOfByte[i];
			stmp = Integer.toHexString(c & 0xFF);
			sb.append(stmp.length() == 1 ? "0" + stmp : stmp);
		}
		return sb.toString().toUpperCase().trim();
	}

	public static byte[] listToArr(ArrayList<Byte> list) {
		byte[] tmp = new byte[list.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = ((Byte) list.get(i)).byteValue();
		}
		return tmp;
	}

	public static ArrayList<Byte> listAddArr(ArrayList<Byte> list, byte[] arr) {
		for (int i = 0; i < arr.length; i++) {
			list.add(Byte.valueOf(arr[i]));
		}
		return list;
	}

	public static byte[] int2byteArray(int num) {
		byte[] result = new byte[4];
		result[0] = ((byte) (num >>> 24));
		result[1] = ((byte) (num >>> 16));
		result[2] = ((byte) (num >>> 8));
		result[3] = ((byte) num);
		return result;
	}

	public static int bytes2int(byte[] bytes) {
		int result = 0;
		if ((bytes == null) || (bytes.length == 0)) {
			return result;
		}
		if (bytes.length < 4) {
			byte[] tmp = new byte[4];
			for (int i = 0; i < bytes.length; i++) {
				tmp[(tmp.length - bytes.length + i)] = bytes[i];
			}
			bytes = tmp;
		}
		if (bytes.length == 4) {
			int a = (bytes[0] & 0xFF) << 24;
			int b = (bytes[1] & 0xFF) << 16;
			int c = (bytes[2] & 0xFF) << 8;
			int d = bytes[3] & 0xFF;
			result = a | b | c | d;
		}
		return result;
	}

	public static String bytes2string(byte[] bytes) {
		return new String(bytes);
	}

	public static long bytes2long(byte[] bytes) {
		long temp = 0L;
		long res = 0L;
		if ((bytes == null) || (bytes.length == 0)) {
			return res;
		}
		if (bytes.length < 8) {
			byte[] tmp = new byte[8];
			for (int i = 0; i < bytes.length; i++) {
				tmp[(tmp.length - bytes.length + i)] = bytes[i];
			}
			bytes = tmp;
		}
		for (int i = 0; i < 8; i++) {
			res <<= 8;
			temp = bytes[i] & 0xFF;
			res |= temp;
		}
		return res;
	}

	public static InputStream bytes2Stream(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		InputStream is = new ByteArrayInputStream(bytes);
		return is;
	}

	public static byte[] stream2Bytes(InputStream is) {
		byte[] bytes = null;
		try {
			int lenth = is.available();
			bytes = new byte[lenth];
			is.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
}
