package database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import protobuf.srl.school.School.DateTime;
import protobuf.srl.school.School.SrlGroup;

public class RequestConverter {

	public static DateTime getProtoFromDate(Calendar cal) {
		DateTime.Builder result = DateTime.newBuilder();
		result.setMillisecond(cal.getTimeInMillis());
		Date date = cal.getTime();
		result.setYear(date.getYear());
		result.setMonth(date.getMonth());
		result.setDay(date.getDay());
		result.setHour(date.getHours());
		result.setMinute(date.getMinutes());
		return result.build();
	}
	
	public static DateTime getProtoFromDate(Date date) {
		DateTime.Builder result = DateTime.newBuilder();
		result.setYear(date.getYear() + 1900);
		result.setMonth(date.getMonth());
		result.setDay(date.getDay());
		result.setHour(date.getHours());
		result.setMinute(date.getMinutes());
		return result.build();
	}

	/**
	 * Creates a proto version of a date from the given milliseconds
	 * @param date
	 * @return
	 */
	public static DateTime getProtoFromMilliseconds(long date) {
	//	Calendar cal = Calendar.getInstance();
	//	cal.setTimeInMillis(date);
		return getProtoFromDate(new Date(date));
	}

	public static Date getDateFromProto(DateTime date) {
		return new Date(date.getMillisecond());
	}

	private static ArrayList<String> GroupToString(SrlGroup group){
		ArrayList<String> inputGroup = new ArrayList<String>();
		inputGroup.add(group.getGroupId());
		return inputGroup;
	}
}