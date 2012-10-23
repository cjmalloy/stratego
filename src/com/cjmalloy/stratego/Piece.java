/*
    This file is part of Stratego.

    Stratego is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Stratego is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Stratego.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.cjmalloy.stratego;

public class Piece implements Comparable<Piece>
{
	private int uniqueID = 0;
	private int color = 0;
	private Rank rank = null;
	private boolean shown = false;
	private boolean known = false;

	public Piece(int id, int c, Rank r) 
	{
		uniqueID = id;
		color = c;
		rank = r;
	}

	public int getColor() 
	{
		return color;
	}

	public Rank getRank() 
	{
		return rank;
	}
	
	public int getID() 
	{
		return uniqueID;
	}
	
	public boolean isShown()
	{
		return shown;
	}
	
	public void setShown(boolean b)
	{
		shown = b;
		if (shown == true)
			known = true;
	}	
	
	public boolean isKnown()
	{
		return known;
	}
	
	public void setKnown(boolean b)
	{
		known = b;
		if (known == false)
			shown = false;
	}

	public int compareTo(Piece p)
	{
		return uniqueID - p.uniqueID;
	}
	
	public boolean equals(Object p)
	{
		return (uniqueID == ((Piece)p).uniqueID);
	}
}
