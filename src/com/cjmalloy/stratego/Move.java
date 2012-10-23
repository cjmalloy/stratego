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

public class Move
{

	private Piece piece = null;
	private Spot from = null;
	private Spot to = null;

	public Move(Piece p, Spot f, Spot t)
	{
		piece = p;
		from = f;
		to = t;
	}

	public Piece getPiece()
	{
		return piece;
	}

	public Spot getFrom()
	{
		return from;
	}

	public Spot getTo()
	{
		return to;
	}
	
	public boolean equals(Object m)
	{
		if (m==null) return false;
		if (piece==((Move)m).piece)
		if (from.equals(((Move)m).from))
		if (to.equals(((Move)m).to))
			return true;
		return false;
	}
}