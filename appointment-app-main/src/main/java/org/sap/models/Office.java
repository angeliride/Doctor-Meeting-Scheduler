package org.sap.models;

public class Office {
	private final int number;
	private final String address;

	public Office(int number, String address) {
		this.number = number;
		this.address = address;
	}

	public int getNumber() {
		return number;
	}

	public String getAddress() {
		return address;
	}

	public static Office copy(Office office) {
		return new Office(office.getNumber(), office.getAddress());
	}

	public String toCsv() {
		return String.format("%d;%s", number, address);
	}
}
