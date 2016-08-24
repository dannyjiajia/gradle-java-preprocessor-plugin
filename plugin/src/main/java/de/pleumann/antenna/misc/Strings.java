/* -----------------------------------------------------------------------------
 * Antenna - An Ant-to-end solution for wireless Java 
 *
 * Copyright (c) 2002-2004 Joerg Pleumann <joerg@pleumann.de>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * -----------------------------------------------------------------------------
 */
package de.pleumann.antenna.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class Strings {

	private Vector strings;

	public Strings() {
		strings = new Vector();
	}

	public int add(String s) {
		int result = size();
		insert(result, s);
		return result;
	}

	public void addStrings(Strings s) {
		for (int i = 0; i < s.size(); i++) {
			add(s.get(i));
		}
	}

	public void assign(Strings s) {
		clear();
		addStrings(s);
	}

	public void clear() {
		strings.clear();
	}

	public void delete(int index) {
		strings.remove(index);
	}

	public boolean equals(Object o) {
		if (!(o instanceof Strings)) {
			return false;
		}

		Strings s = (Strings) o;
		if (s.size() != size()) {
			return false;
		}

		for (int i = 0; i < s.size(); i++) {
			if (!(s.get(i).equals(get(i)))) {
				return false;
			}
		}
		return true;
	}

	public void exchange(int index1, int index2) {
		exchangeItem(index1, index2);
	}

	private void exchangeItem(int i, int j) {
		Object s = strings.get(i);
		strings.set(i, strings.get(j));
		strings.set(j, s);
	}

	public String get(int index) {
		return (String) strings.get(index);
	}

	public int indexOf(String s) {
		for (int i = 0; i < size(); i++) {
			if (get(i).equals(s)) {
				return i;
			}
		}
		return -1;
	}

	public void insert(int index, String s) {
		strings.insertElementAt(s, index);
	}

	public void move(int oldIndex, int newIndex) {
		String s = get(oldIndex);
		delete(oldIndex);
		insert(newIndex, s);
	}

	public void loadFromFile(String filename) throws IOException {
		loadFromFile(new File(filename));
	}
	public void loadFromFile(File filename) throws IOException {
		loadFromStream(new FileInputStream(filename));
	}

	public void loadFromFile(File filename, String encoding) throws IOException, UnsupportedEncodingException {
		loadFromStream(new FileInputStream(filename), encoding);
	}

	public void loadFromStream(InputStream stream) throws IOException {
		clear();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		int i = 0;
		String s = reader.readLine();
		while (s != null) {
			insert(i, s);
			i++;
			s = reader.readLine();
		}
		reader.close();
	}

	public void loadFromStream(InputStream stream, String encoding) throws IOException, UnsupportedEncodingException {
		clear();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
		int i = 0;
		String s = reader.readLine();
		while (s != null) {
			insert(i, s);
			i++;
			s = reader.readLine();
		}
		reader.close();
	}

	public void saveToFile(String filename) throws IOException {
		saveToStream(new FileOutputStream(filename));
	}

	public void saveToFile(String filename, String encoding) throws IOException, UnsupportedEncodingException {
		saveToStream(new FileOutputStream(filename), encoding);
	}

	public void saveToStream(OutputStream stream) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

		for (int i = 0; i < size(); i++) {
			writer.write(get(i));
			writer.newLine();
		}
		writer.close();
	}

	public void saveToStream(OutputStream stream, String encoding) throws IOException, UnsupportedEncodingException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, encoding));

		for (int i = 0; i < size(); i++) {
			writer.write(get(i));
			writer.newLine();
		}
		writer.close();
	}

	public void set(int index, String s) {
		strings.set(index, s);
	}

	public int size() {
		return strings.size();
	}

	/**
	 * Gets the key stored in the given line (everything before the first ':'),
	 * or null if the line doesn't contain a key.
	 */
	public String getName(int i) {
		String result = get(i);
		int p = result.indexOf(':');
		if (p != -1) {
			result = result.substring(0, p);
		}
		else
			result = null;

		return result;
	}

	/**
	 * Gets the value belonging to the given key, or null if the key is not
	 * found.
	 */
	public String getValue(String name) {
		int i = indexOfName(name);
		if (i != -1) {
			String result = get(i);
			i = result.indexOf(':');
			result = result.substring(i + 1);
			return result.trim();
		}
		else {
			return null;
		}
	}

	/**
	 * Finds a key in a JAD file. Returns either the index at which the
	 * key was found, or -1 if the line doesn't exist.
	 */
	public int indexOfName(String name) {
		String s = name.toLowerCase() + ':';
		int i = 0;
		while (i < size()) {
			if (get(i).toLowerCase().startsWith(s)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Sets the value of the given key, replacing a previous definition if one
	 * exists.
	 */
	public void setValue(String name, String value) {
		int i = indexOfName(name);
		if (i == -1) {
			if ((value != null) && (!value.equals(""))) {
				add(name + ": " + value);
			}
		}
		else {
			if ((value != null) && (!value.equals(""))) {
				set(i, name + ": " + value);
			}
			else {
				delete(i);
			}
		}
	}
	
	public Vector getVector()
	{
		return strings;
	}
}
