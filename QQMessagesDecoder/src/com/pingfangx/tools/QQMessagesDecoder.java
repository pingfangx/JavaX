package com.pingfangx.tools;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 查看安卓手机QQ中的聊天记录
 * 来自http://bbs.pediy.com/thread-215245.htm
 * 作者提供的软件自己用了显示不了，于是提取了自己写一下
 *
 */
public class QQMessagesDecoder {

	@Option(name = "-h", aliases = { "-help", "--help" }, help = true, usage = "显示帮助")
	private boolean help = false;

	@Option(name = "-d", aliases = "-database", required = true, usage = "数据库")
	private String database;

	@Option(name = "-i", aliases = "-imei", required = true, usage = "IMEI")
	private String imei;

	@Option(name = "-f", aliases = "-friend", required = true, usage = "要查看的好友qq")
	private String friend;

	public static void main(String[] args) throws IOException {
		new QQMessagesDecoder().doMain(args);
	}

	public void doMain(String[] args) throws IOException {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
			if (help) {
				parser.printUsage(System.err);
				return;
			}
			try {
				decodeQQMessages(database, imei, friend);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}

	}

	/*
	 * 解密qq消息
	 */
	private void decodeQQMessages(String databasePath, String imei, String friend) {
		String selfQQ = databasePath.toLowerCase();
		selfQQ = selfQQ.replace("\\", "/");
		selfQQ = selfQQ.split(".db")[0];
		// 前面要加/
		selfQQ = "/" + selfQQ;
		JieMi jiemi = new JieMi(selfQQ, imei);
		if (friend.length() < 20) {
			friend = String.format("mr_friend_%s_New", getMD5(friend.getBytes()));
		}
		List<Object[]> messages = Tools.getMessageByQQ(jiemi, friend);
		for (Object[] objectArray : messages) {
			String line = "";
			for (Object object : objectArray) {
				line += "\t" + object.toString();
			}
			System.out.println(line);
		}
	}

	/**
	 * md5 来自http://blog.csdn.net/gxy3509394/article/details/7409284
	 */
	public static String getMD5(byte[] source) {
		String s = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };// 用来将字节转换成16进制表示的字符
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest();// MD5 的计算结果是一个 128 位的长整数，
			// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2];// 每个字节用 16 进制表示的话，使用两个字符， 所以表示成 16
			// 进制需要 32 个字符
			int k = 0;// 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) {// 从第一个字节开始，对 MD5 的每一个字节// 转换成 16
				// 进制字符的转换
				byte byte0 = tmp[i];// 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];// 取字节中高 4 位的数字转换,// >>>
				// 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf];// 取字节中低 4 位的数字转换

			}
			s = new String(str);// 换后的结果转换为字符串

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return s;
	}
}
