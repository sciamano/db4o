package demo.objectmanager.model;

import com.db4o.ObjectContainer;
import com.db4o.Db4o;
import com.db4o.objectmanager.api.util.GenericObjectUtil;
import com.db4o.reflect.generic.GenericClass;
import com.db4o.reflect.generic.GenericReflector;
import com.db4o.reflect.generic.GenericField;
import com.db4o.reflect.ReflectField;
import com.db4o.reflect.Reflector;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.math.BigDecimal;

/**
 * User: treeder
 * Date: Aug 30, 2006
 * Time: 11:59:28 AM
 */
public class DemoPopulator {
	/**
	 * Filename for demo database
	 */
	private static final String DB_FILE = "demo.db";
	/**
	 * Number of top level objects to make
	 */
	private static final int NUMBER_TO_MAKE = 1000;

	private ObjectContainer db;
	private static int idGen;
	private String fileName = DB_FILE;

	public static void main(String[] args) {
		DemoPopulator p = new DemoPopulator();
		p.run();
	}

	public void run() {
		ObjectContainer db = getDb();
		int ageCounter = 0;
		List<Contact> last10 = new ArrayList<Contact>();
		Calendar birthCal = Calendar.getInstance();
		for(int i = 0; i < NUMBER_TO_MAKE; i++) {
			Contact c = new Contact();
			c.setId(nextId());
			c.setFirstName("Contact " + i);
			c.setLastName("Lastname");
			c.setAge(++ageCounter);
			if(ageCounter >= 100) {
				ageCounter = 0;
			}
			Date now = new Date();
			c.setCreated(now);
			c.setIncome(i * 1000.01);
			c.setBirthDate(birthCal.getTime());
			c.setGender(i % 2 == 0 ? 'm' : 'f');
			birthCal.add(Calendar.DAY_OF_YEAR, -1);
			c.setArbitraryObject(getArbitraryObject(i));
			addAddresses(c);
			addEmails(c);
			addFriends(c, last10);
			c.setNote(new Note("This note is about " + c.getFirstName() + ". ???? ??????????? ??????????? ??? ??, ?????? ??????? ?? ???. ?? ??? ??????? ??????? ?????????. ??? ????? ???????? ????????? ??, ??? ???????? ?????????? ??. ??? ???????? ????????? ??. ??? ???????? ????????? ???????????? ??, ?? ???????? ????????? ??????????? ???.\n" +
					"\n" +
					"??? ?? ????? ????????, ?????? ??????? ????????? ?? ???. ???? ?????? ???????? ??? ??, ??? ?????? ???????? ??. ?? ??????? ??????????? ??????????? ???, ????? ????? ????????? ?? ???. ?? ???? ?????? ???, ?? ??? ???? ??????.\n" +
					"\n" +
					"????? ?????? ??????????? ??? ??, ?? ??? ???? ??????????, ?? ??? ???? ???????. ?? ??? ?????? ??????????. ??? ??????? ?????????? ?????????? ??, ?????? ??????? ???????? ?? ???, ????? ???????? ????????? ?? ???. ?? ??????? ????????? ????????? ???. ????? ???????? ?? ???, ??? ??????? ???????? ?????????? ??. ???????? ??????????? ?????????????? ?? ???, ??? ?? ??????? ??????? ?????????, ??? ???? ?????????? ??.\n" +
					"\n" +
					"??? ?? ???? ?????????? ???????????, ??? ?? ?????????? ????????????????, ???? ???? ?? ???. ?? ?????? ????????? ???. ????? ??????????? ?? ???, ?? ?????? ?????? ???, ?? ?????? ????????? ???. ???? ????? ?????? ??? ??. ??? ????????? ?????????? ?????????? ??.\n" +
					"\n" +
					"?? ??? ???? ??????????, ??? ?? ????? ????????? ??????????. ?? ??? ????????? ????????????, ????? ???????? ?? ???. ?? ?????? ??????? ??????????? ???. ??? ???? ?????? ???????????? ??, ??? ?? ????????? ??????????.\n" +
					"\n" +
					"???? ????? ?? ???, ??? ????? ?????????? ??????????? ??. ?? ??? ???????? ????????? ??????????, ??? ???? ????? ?????????? ??. ??????? ?????????? ??????????? ?? ???, ?? ??????? ???????? ???, ?? ????????? ????????? ??????????????? ???. ????? ?????? ????????? ?? ???, ?? ???? ???? ???, ????? ????????? ??????????? ??? ??. ??? ?? ????? ???????.\n" +
					"\n" +
					"?? ??????? ???????? ?????????????? ???. ???? ?????? ???????? ?? ???, ??? ?? ??????? ?????????. ?????? ??????????????? ??? ??, ?? ??? ??????? ??????? ????????, ?? ????? ????????? ???????????? ???. ?? ???? ?????????? ???. ?? ??? ???? ??????? ??????????, ?? ?????? ???????? ?????????? ???, ??? ?? ????? ???????????.\n" +
					"\n" +
					"??????? ???????? ?? ???, ?? ??? ????? ??????? ????????. ?? ???????? ????????? ???, ??? ??????? ??????? ???????????? ??, ?? ??? ???? ???? ?????. ?? ?????? ??????? ???, ??? ?? ???? ??????? ?????????. ???? ????????? ?? ???, ?? ??? ????? ?????? ????????, ??????? ?????????? ??? ??.\n" +
					"\n" +
					"??? ?????????? ??????????? ??????????? ??. ??? ?????? ???????? ??, ?????? ???????? ?? ???. ??? ???????? ?????????? ??????????? ??, ??? ?? ??????? ????????, ?? ??? ???? ????????? ?????????. ?? ?????? ???????? ????????? ???. ????? ????? ??????? ?? ???, ?? ?????? ????????? ????????? ???, ?? ?????? ?????????? ???????????? ???.\n" +
					"\n" +
					"??? ????? ?????????? ??. ??? ????? ??????????? ??, ?? ??? ??????????? ???????????, ??? ?????? ??????? ?????????? ??. ???? ????? ?????? ?? ???. ??? ????? ?????????? ??. ?? ??? ???????? ????????? ??????????????.\n" +
					"      ", false));

			db.set(c);
			last10.add(0, c);
			if(last10.size() > 10) {
				last10.remove(10);
			}
			if(i % 1000 == 0) {
				db.commit();
			}
		}

		db.set(Collections.forDemo());
		Inheritance.forDemo(db);

		fillAllDataTypes(db);

		createGenericObjects(db);

		db.commit();
		db.close();
	}

	private void fillAllDataTypes(ObjectContainer db) {
		for(int i = 0; i < NUMBER_TO_MAKE; i++) {
			AllDataTypes allDataTypes = new AllDataTypes();
			allDataTypes.setABoolean(i % 2 == 0 ? true : false);
			allDataTypes.setWBoolean(new Boolean(allDataTypes.isABoolean()));
			allDataTypes.setAByte((byte) 1);
			allDataTypes.setWByte(new Byte(allDataTypes.getAByte()));
			allDataTypes.setAChar('z');
			allDataTypes.setWChar(new Character(allDataTypes.getAChar()));
			allDataTypes.setAShort((short) 8);
			allDataTypes.setWShort(new Short(allDataTypes.getAShort()));
			allDataTypes.setAInt(123);
			allDataTypes.setWInt(new Integer(allDataTypes.getAInt()));
			allDataTypes.setALong(4567l);
			allDataTypes.setWLong(new Long(allDataTypes.getALong()));
			allDataTypes.setAFloat(12.34f);
			allDataTypes.setWFloat(new Float(allDataTypes.getAFloat()));
			allDataTypes.setADouble(56.78);
			allDataTypes.setWDouble(new Double(allDataTypes.getADouble()));
			allDataTypes.setDate(new Date());
			db.set(allDataTypes);
		}
	}

	public static Object getArbitraryObject(int i) {
		int mod = i % 7;
		switch(mod) {
			case 0:
				return new Boolean(true);
			case 1:
				return new Integer(123);
			case 2:
				return new BigDecimal("123.456");
			case 3:
				return new Double(123.456);
			case 4:
				return new Date();
			case 5:
				return new String("arbitrary string");
			case 7:
				return new Object();
		}
		return null;
	}

	private Integer nextId() {
		return new Integer(++idGen);
	}

	private void addFriends(Contact c, List<Contact> last10) {
		c.setFriends(last10);
	}

	private synchronized ObjectContainer getDb() {
		if(db == null) {
			// delete old file if it exists
			File f = new File(DB_FILE);
			if(f.exists()) f.delete();
			//Db4o.configure().objectClass(Address.class).objectField("street").indexed(true);
			db = Db4o.openFile(DB_FILE);
		}
		return db;
	}

	private void addEmails(Contact c) {
		for(int i = 0; i < 10; i++) {
			c.addEmail(new EmailAddress("name@somewhere" + i + ".com"));
		}
	}

	private void addAddresses(Contact c) {
		for(int i = 0; i < 5; i++) {
			c.addAddress(new Address(nextId(), c, i + " street", "San Francisco", "CA", "90210", (i == 0), new Boolean((i == 2))));
		}
	}

	public String getFileName() {
		return fileName;
	}

	private void createGenericObjects(ObjectContainer db) {
		GenericClass carClass = makeGenericCarClass();
		ReflectField surname = carClass.getDeclaredField("name");
		ReflectField birthdate = carClass.getDeclaredField("buildDate");
		ReflectField mpg = carClass.getDeclaredField("mpg");
		for(int i = 0; i < 100; i++) {
			Object car = carClass.newInstance();
			surname.set(car, "Jaguar");
			birthdate.set(car, new Date());
			mpg.set(car, i);
			db.set(car);
		}
		db.commit();
	}

	private GenericClass makeGenericCarClass(){
		GenericClass result = GenericObjectUtil.makeGenericClass("com.acme.Car");
		result.initFields(fields(result.reflector()));
		return result;
	}

	private GenericField[] fields(Reflector reflector) {
		return new GenericField[]{
				new GenericField("name", reflector.forClass(String.class),
						false, false, false),
				new GenericField("buildDate", reflector.forClass(Date.class),
						false, false, false),
				new GenericField("mpg", reflector.forClass(Integer.class), true, false, false),
		};
	}

}
