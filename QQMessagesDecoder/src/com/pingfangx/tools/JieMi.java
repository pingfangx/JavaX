package com.pingfangx.tools;

import java.io.UnsupportedEncodingException;

public class JieMi {
	private String IMEI;
	private String QQ;
	private int lenght;

	public JieMi() {
	}

	public JieMi(String QQ) {
		this.QQ = QQ;
	}

	public JieMi(String QQ, String IMEI) {
		this.QQ = QQ;
		this.IMEI = IMEI;
		this.lenght = IMEI.length();
	}

	public String getIMEI() {
		return this.IMEI;
	}

	public void setIMEI(String iMEI) {
		this.IMEI = iMEI;
		if (iMEI == null)
			this.lenght = 0;
		else
			this.lenght = this.IMEI.length();
	}

	public String getQQ() {
		return this.QQ;
	}

	public void setQQ(String qQ) {
		this.QQ = qQ;
	}

	public String jiemi(byte[] ms) {
		byte[] tmp = (byte[]) ms.clone();
		for (int i = 0; i < ms.length; i++) {
			tmp[i] = ((byte) (ms[i] ^ this.IMEI.charAt(i % this.lenght)));
		}
		String rtn = "";
		try {
			rtn = new String(tmp, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return rtn;
	}

	public String jiemi1(byte[] ms, String qq) {
		if (ms == null) {
			return null;
		}
		byte[] tmp = (byte[]) ms.clone();
		for (int i = 0; i < ms.length; i++) {
			tmp[i] = ((byte) (ms[i] ^ qq.charAt(i % qq.length())));
		}
		String rtn = "";
		try {
			rtn = new String(tmp, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return rtn;
	}

	public String jiemi2(byte[] ms) throws UnsupportedEncodingException {
		byte[] imei = this.IMEI.getBytes("utf-8");
		String rtn = "";
		if (ms == null) {
			return rtn;
		}

		int j = 0;

		int ttt = 0;
		byte[] tmp = (byte[]) ms.clone();
		for (int i = 0; i < ms.length; i++) {
			ttt = ms[i] & 0xFF;

			if (((ttt >= 192) && (ttt <= 223)) || ((ttt >= 224) && (ttt <= 239))) {
				if (imei[(j % this.lenght)] >= 65) {
					tmp[(i + 1)] = ((byte) ((ms[(i + 1)] ^ 0x1) & 0xFF));
					tmp[(i + 2)] = ((byte) ((ms[(i + 2)] ^ imei[(j % this.lenght)] - 64) & 0xFF));
					i += 2;
					j++;
				} else {
					i += 2;
				}
			} else {
				tmp[i] = ((byte) ((ms[i] ^ imei[(j % this.lenght)]) & 0xFF));
				j++;
			}
		}
		rtn = new String(tmp, "utf-8");

		return rtn;
	}

	public byte[] jiemi3(byte[] ms) {
		byte[] tmp = (byte[]) ms.clone();
		for (int i = 0; i < ms.length; i++) {
			tmp[i] = ((byte) (ms[i] ^ this.IMEI.charAt(i % this.lenght)));
		}
		return tmp;
	}

	public String jiemi4(byte[] encode, String bys) throws UnsupportedEncodingException {
		String rtn = "";
		if (encode == null) {
			return rtn;
		}
		byte[] decode = bys.getBytes("utf-8");
		int len = encode.length > decode.length ? decode.length : encode.length;

		int j = 0;

		int ttt = 0;
		byte[] tmp = new byte[len];
		for (int i = 0; i < len; i++) {
			ttt = encode[i] & 0xFF;
			if (((ttt >= 192) && (ttt <= 223)) || ((ttt >= 224) && (ttt <= 239))) {
				if (encode[(i + 1)] != decode[(i + 1)])
					tmp[j] = ((byte) ((encode[(i + 2)] ^ decode[(i + 2)]) + 64 & 0xFF));
				else {
					tmp[j] = ((byte) ((encode[(i + 2)] ^ decode[(i + 2)]) & 0xFF));
				}
				i += 2;
				j++;
			} else {
				tmp[j] = ((byte) ((encode[i] ^ decode[i]) & 0xFF));
				j++;
			}
		}
		tmp = MyByte.cutOutByte(tmp, j - 1);
		rtn = new String(tmp, "utf-8");

		return rtn;
	}
}
