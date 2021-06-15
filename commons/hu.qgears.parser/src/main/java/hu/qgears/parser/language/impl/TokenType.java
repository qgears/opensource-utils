package hu.qgears.parser.language.impl;

import hu.qgears.parser.language.ITokenType;

public class TokenType implements ITokenType {
	int id;
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "tokentype: " + getName() + " " + getId();
	}

	public TokenType(String name) {
		super();
		this.name = name;
	}
}
