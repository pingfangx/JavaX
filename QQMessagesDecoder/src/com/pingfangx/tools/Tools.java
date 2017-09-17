package com.pingfangx.tools;

import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Tools {
	public static final String getTables = "SELECT tbl_name FROM sqlite_master WHERE type='table' and name like 'mr_friend_%';";

	public static Connection get_con(String qq) {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + qq + ".db");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static ArrayList<String> get_table(String qq) {
		Connection conn = get_con(qq);
		ArrayList tables = new ArrayList();
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat
					.executeQuery("SELECT tbl_name FROM sqlite_master WHERE type='table' and name like 'mr_friend_%';");
			while (rs.next()) {
				tables.add(rs.getString(1));
			}
			rs.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tables;
	}

	public static String creackIMEIByQQ(String qq) {
		String IMEI = "";
		Connection conn = get_con(qq);
		ArrayList tables = get_table(qq);
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = null;
			for (int i = 0; i < tables.size(); i++) {
				rs = stat.executeQuery("SELECT * FROM " + (String) tables.get(i) + ";");
				if (rs.next()) {
					break;
				}
			}
			IMEI = new JieMi(qq).jiemi1(rs.getBytes("selfuin"), qq);
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return IMEI;
	}

	public static String crackalias(JieMi jiemi, String id, String alias) {
		Connection conn = get_con(jiemi.getQQ());
		String imei = "";
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM Friends where _id=" + id + ";");
			if (rs.next()) {
				imei = jiemi.jiemi1(rs.getBytes("alias"), alias);
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (imei.length() > 15) {
			imei = imei.substring(0, 15);
		}
		return imei;
	}

	public static ArrayList<Object[]> getCrackAlias(JieMi jiemi) {
		Connection conn = get_con(jiemi.getQQ());
		ArrayList datas = new ArrayList();
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM Friends;");
			while (rs.next()) {
				boolean tmp = rs.getBytes("alias").length >= 15;
				if (tmp) {
					Object[] data = new Object[3];
					data[0] = Integer.valueOf(rs.getInt("_id"));
					data[1] = jiemi.jiemi(rs.getBytes("uin"));
					data[2] = Boolean.valueOf(tmp);
					datas.add(data);
				}
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datas;
	}

	public static ArrayList<Object[]> getGroups(JieMi jiemi) {
		Connection conn = get_con(jiemi.getQQ());
		ArrayList datas = new ArrayList();

		Object[] no = new Object[3];
		no[0] = Integer.valueOf(0);
		no[1] = "默认分组";
		no[2] = Integer.valueOf(-1);
		datas.add(no);
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM Groups;");
			while (rs.next()) {
				Object[] data = new Object[3];
				data[0] = Integer.valueOf(rs.getInt("_id"));
				data[1] = jiemi.jiemi2(rs.getBytes("group_name"));

				data[2] = Integer.valueOf(rs.getInt("group_id"));
				datas.add(data);
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datas;
	}

	public static ArrayList<Object[]> getFriendsByGroup(JieMi jiemi, String groupid) {
		Connection conn = get_con(jiemi.getQQ());
		ArrayList datas = new ArrayList();
		String query = "SELECT * FROM Friends where groupid=" + groupid + ";";
		if (groupid.equals("-1"))
			query = "SELECT tbl_name FROM sqlite_master WHERE type='table' and name like 'mr_friend_%';";
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(query);
			while (rs.next()) {
				Object[] data = new Object[5];
				if (groupid.equals("-1")) {
					data[0] = "--";

					data[1] = rs.getString("tbl_name");

					data[2] = "--";

					data[3] = "--";

					data[4] = "--";
				} else {
					data[0] = Integer.valueOf(rs.getInt("_id"));

					data[1] = jiemi.jiemi(rs.getBytes("uin"));

					data[2] = jiemi.jiemi2(rs.getBytes("name"));

					data[3] = jiemi.jiemi2(rs.getBytes("remark"));

					data[4] = jiemi.jiemi(rs.getBytes("alias"));
				}
				datas.add(data);
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datas;
	}

	public static ArrayList<Object[]> getMessageByQQ(JieMi jiemi, String qq) {
		Connection conn = get_con(jiemi.getQQ());
		ArrayList datas = new ArrayList();
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM " + qq + " order by time;");
			while (rs.next()) {
				Object[] data = new Object[5];

				data[0] = Integer.valueOf(rs.getInt("_id"));

				String senderuin = jiemi.jiemi(rs.getBytes("senderuin"));
				data[1] = senderuin;

				String selfuin = jiemi.jiemi(rs.getBytes("selfuin"));
				String frienduin = jiemi.jiemi(rs.getBytes("frienduin"));
				data[2] = (senderuin.equals(selfuin) ? frienduin : selfuin);

				data[3] = jiemi.jiemi(rs.getBytes("msgdata"));

				data[4] = convertTime(rs.getString("time"));
				datas.add(data);
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			System.out.println("查询数据库失败");
		}

		return datas;
	}

	public static ArrayList<Object[]> getTroops(JieMi jiemi) {
		Connection conn = get_con(jiemi.getQQ());
		ArrayList datas = new ArrayList();
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM TroopInfo;");
			while (rs.next()) {
				Object[] data = new Object[3];
				data[0] = Integer.valueOf(rs.getInt("_id"));
				data[1] = jiemi.jiemi2(rs.getBytes("troopname"));
				data[2] = jiemi.jiemi(rs.getBytes("troopuin"));
				datas.add(data);
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datas;
	}

	public static ArrayList<Object[]> getFriendsByTroop(JieMi jiemi, String troopid) {
		Connection conn = get_con(jiemi.getQQ());
		ArrayList datas = new ArrayList();
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat
					.executeQuery("SELECT * FROM TroopMemberInfo a,TroopInfo b where a.troopuin=b.troopuin and b._id="
							+ troopid + ";");
			while (rs.next()) {
				Object[] data = new Object[5];

				data[0] = jiemi.jiemi(rs.getBytes("troopuin"));

				data[1] = jiemi.jiemi(rs.getBytes("memberuin"));

				data[2] = jiemi.jiemi2(rs.getBytes("troopnick"));
				datas.add(data);
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datas;
	}

	public static Object[][] getCells(ArrayList<Object[]> datas) {
		Object[][] cells = new Object[datas.size()][];
		for (int i = 0; i < datas.size(); i++) {
			cells[i] = ((Object[]) datas.get(i));
		}
		return cells;
	}

	public static String convertTime(String time) {
		long tmp = Long.valueOf(time + "000").longValue();
		Date date = new Date(tmp);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 E HH时mm分ss秒");
		return format.format(date);
	}

	public static String Md5(String str) {
		String rtn = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(str.getBytes());
			rtn = new BigInteger(1, md5.digest()).toString(16);
			rtn = rtn.toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return rtn;
	}

	public static String getTroopInfo(JieMi jiemi, String troopid) {
		Connection conn = get_con(jiemi.getQQ());
		String info = "";
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT troopmemo FROM TroopInfo where _id=" + troopid + ";");
			if (rs.next()) {
				info = jiemi.jiemi2(rs.getBytes("troopmemo"));
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	public static byte[] getTroopInfoBytes(JieMi jiemi, String troopid) {
		Connection conn = get_con(jiemi.getQQ());
		byte[] bys = null;
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT troopmemo FROM TroopInfo where _id=" + troopid + ";");
			if (rs.next()) {
				bys = rs.getBytes("troopmemo");
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bys;
	}
}
