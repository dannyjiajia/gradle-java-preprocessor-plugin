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
package de.pleumann.antenna.post;

import java.io.*;
import java.util.Vector;

public class ClassFile {

	private class Entry {
		int tag;
		byte[] data;

		Entry(int tag, byte[] data) {
			this.tag = tag;
			this.data = data;
		}
	}

	private static final int[] sizes =
		new int[] { 0, 0, 0, 4, 4, 8, 8, 2, 2, 4, 4, 4, 4 };

	private Vector classes = new Vector();

	public ClassFile(InputStream input) throws IOException {
		DataInputStream data = new DataInputStream(input);

		int[] magic = new int[4];
		magic[0] = data.readUnsignedByte();
		magic[1] = data.readUnsignedByte();
		magic[2] = data.readUnsignedByte();
		magic[3] = data.readUnsignedByte();

		if ((magic[0] != 0xCA)
			|| (magic[1] != 0xFE)
			|| (magic[2] != 0xBA)
			|| (magic[3] != 0xBE)) {
			throw new IOException("Wrong magic - not a class file");
		}

		data.skip(4);

		int numConstants = data.readUnsignedShort();

		Vector entries = new Vector();

		entries.add(new Entry(0, new byte[0]));

		for (int i = 1; i < numConstants; i++) {
			int tag = data.readUnsignedByte();

			int size = sizes[tag];
			if (size == 0)
				size = data.readUnsignedShort();

			byte[] bytes = new byte[size];
			data.readFully(bytes);

			entries.add(new Entry(tag, bytes));

			if ((tag == 5) || (tag == 6)) {
				entries.add(new Entry(tag, new byte[0]));
				i++;
			}
		}

		for (int i = 1; i < entries.size(); i++) {
			Entry entry = (Entry) entries.elementAt(i);

			if (entry.tag == 7) {
				int hi = entry.data[0] & 0xFF;
				int lo = entry.data[1] & 0xFF;
				Entry name = (Entry) entries.elementAt(hi << 8 | lo);

                String cls = new String(name.data).replace('/', '.');
				if (!cls.startsWith("["))
					classes.addElement(cls);
			}

		}
	}

	public int getRequiredClassCount() {
		return classes.size();
	}

	public String getRequiredClass(int index) {
		return (String) classes.elementAt(index);
	}
}
