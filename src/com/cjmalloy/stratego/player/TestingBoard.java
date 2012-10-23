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

package com.cjmalloy.stratego.player;

import com.cjmalloy.stratego.Board;
import com.cjmalloy.stratego.Move;
import com.cjmalloy.stratego.Piece;
import com.cjmalloy.stratego.Spot;

public class TestingBoard extends Board
{
	public TestingBoard() {}
	
	public TestingBoard(Board t)
	{
		super(t);
	}

	public boolean move(Move m)
	{
		if (!super.move(m))
			return attack(m);
		return false;
	}

	public void undo(Piece fp, Spot f, Piece tp, Spot t)
	{
		setPiece(fp, f);
		setPiece(tp, t);
		
	}
	
	public boolean validMove(Spot f, Spot t)
	{
		if (super.validAttack(new Move(getPiece(f), f, t)))
			return true;
		
		return super.validMove(new Move(getPiece(f), f, t));
	}
	
	@Override
	protected void setKnown(Piece p, boolean b){}

	@Override
	protected void setShown(Piece p, boolean b){}
}
