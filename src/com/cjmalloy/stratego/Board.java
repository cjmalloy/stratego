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

import java.util.ArrayList;
import java.util.Collections;

public class Board
{
	protected static class UniqueID
	{
		private static int id = 0;
		
		static public int get()
		{
			id++;
			return id;
		}
	}
	
	public static final int RED  = 0;
	public static final int BLUE = 1;
	public static final Spot IN_TRAY = new Spot(-1, -1);
	public static final Piece INVALID = new Piece(-1, -1, Rank.WATER);
	
	protected Piece[][] grid = new Piece[10][10];
	protected ArrayList<Piece> tray = new ArrayList<Piece>();
	protected ArrayList<Move> recentMoves = new ArrayList<Move>();
	
	public Board()
	{
		//create lakes
		grid[2][4] = new Piece(UniqueID.get(), -1, Rank.WATER);
		grid[3][4] = new Piece(UniqueID.get(), -1, Rank.WATER);
		grid[2][5] = new Piece(UniqueID.get(), -1, Rank.WATER);
		grid[3][5] = new Piece(UniqueID.get(), -1, Rank.WATER);
		grid[6][4] = new Piece(UniqueID.get(), -1, Rank.WATER);
		grid[7][4] = new Piece(UniqueID.get(), -1, Rank.WATER);
		grid[6][5] = new Piece(UniqueID.get(), -1, Rank.WATER);
		grid[7][5] = new Piece(UniqueID.get(), -1, Rank.WATER);
		
		//create pieces
		for (int i=RED;i<=BLUE;i++)
		{
			tray.add(new Piece(UniqueID.get(), i, Rank.FLAG));
			tray.add(new Piece(UniqueID.get(), i, Rank.SPY));
			tray.add(new Piece(UniqueID.get(), i, Rank.ONE));
			tray.add(new Piece(UniqueID.get(), i, Rank.TWO));
			for (int j=0;j<2;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.THREE));
			for (int j=0;j<3;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.FOUR));
			for (int j=0;j<4;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.FIVE));
			for (int j=0;j<4;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.SIX));
			for (int j=0;j<4;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.SEVEN));
			for (int j=0;j<5;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.EIGHT));
			for (int j=0;j<8;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.NINE));
			for (int j=0;j<6;j++)
				tray.add(new Piece(UniqueID.get(), i, Rank.BOMB));
		}
		Collections.sort(tray);
	}
	
	public Board(Board b)
	{
		for (int i=0;i<10;i++)
		for (int j=0;j<10;j++)
			grid[i][j] = b.getPiece(i, j);
			
		tray.addAll(b.tray);
		recentMoves.addAll(b.recentMoves);
	}

	public boolean add(Piece p, Spot s)
	{
		if (p.getColor() == Settings.topColor)
		{
			if(s.getY() > 3)
				return false;
		}
		else
		{
			if (s.getY() < 6)
				return false;
		}
			
		if (getPiece(s) == null)
		{
			setKnown(p, false);
			grid[s.getX()][s.getY()] = p;
			tray.remove(p);
			return true;
		}
		return false;
	}
	
	public boolean attack(Move m)
	{
		if (validAttack(m))
		{
			setShown(m.getPiece(), true);
			if (!Settings.bNoShowDefender || m.getPiece().getRank().equals(Rank.NINE))
				setShown(getPiece(m.getTo()), true);
			
			int result = winFight(m.getPiece(), getPiece(m.getTo()));
			if (result == 1)
			{
				remove(m.getTo());
				setPiece(m.getPiece(), m.getTo());
				setPiece(null, m.getFrom());
			}
			else if (result == -1)
			{
				remove(m.getFrom());
				remove(m.getTo());
			}
			else 
			{
				clearRecentMoves(getPiece(m.getTo()));
				
				if (Settings.bNoMoveDefender ||
						getPiece(m.getTo()).getRank().equals(Rank.BOMB))
					remove(m.getFrom());
				else if (m.getPiece().getRank().equals(Rank.NINE))
					scoutLose(m);
				else
				{
					remove(m.getFrom());
					setPiece(getPiece(m.getTo()), m.getFrom());
					setPiece(null, m.getTo());
				}
			}
			return true;
		}
		return false;
	}

	public int checkWin()
	{
		int flags = 0;
		int flagColor = -1;
		for (int i=0;i<10;i++)
		for (int j=0;j<10;j++)
			if (getPiece(i, j) != null)
			if (getPiece(i, j).getRank().equals(Rank.FLAG))
			{
				flagColor = grid[i][j].getColor();
				flags++;
			}
		if (flags!=2)
			return flagColor;
		
		for (int k=0;k<2;k++)
		{
			int movable = 0;
			for (int i=0;i<10;i++)
			for (int j=0;j<10;j++)
			{
				if (getPiece(i, j) != null)
				if (getPiece(i, j).getColor() == k)
				if (!getPiece(i, j).getRank().equals(Rank.FLAG))
				if (!getPiece(i, j).getRank().equals(Rank.BOMB))
				if (getPiece(i+1, j) == null ||
					getPiece(i+1, j).getColor() == (k+1)%2||
					getPiece(i, j+1) == null ||
					getPiece(i, j+1).getColor() == (k+1)%2||
					getPiece(i-1, j) == null ||
					getPiece(i-1, j).getColor() == (k+1)%2||
					getPiece(i, j-1) == null ||
					getPiece(i, j-1).getColor() == (k+1)%2)
					
					movable++;
			}
			
			if (movable==0)
				return (k+1)%2;
		}
		
		return -1;
	}
	
	public void clear()
	{
		for (int i=0;i<10;i++)
		for (int j=0;j<10;j++)
			if (grid[i][j] != null)
			if (!grid[i][j].getRank().equals(Rank.WATER))
			{
				tray.add(grid[i][j]);
				grid[i][j] = null;
			}
		
		Collections.sort(tray);
		
		for (Piece p: tray)
			setKnown(p, false);
	}
	
	private void clearRecentMoves(Piece p)
	{
		for (int i=0;i<recentMoves.size();i++)
			if (recentMoves.get(i).getPiece() == p)
				recentMoves.remove(i);
				
	}

	public Piece getPiece(int x, int y)
	{
		if (x<0||x>9||
			y<0||y>9)
			return INVALID;
		return grid[x][y];
	}
	
	public Piece getPiece(Spot s)
	{
		if (s.getX()<0||s.getX()>9||
			s.getY()<0||s.getY()>9)
			return INVALID;
		return grid[s.getX()][s.getY()];
	}

	
	public Piece getTrayPiece(int i)
	{
		return tray.get(i);
	}
	
	public int getTraySize()
	{
		return tray.size();
	}
	
	public void showAll()
	{
		for (int i=0;i<10;i++)
		for (int j=0;j<10;j++)
			if (grid[i][j] != null)
				setShown(grid[i][j], true);
		for (Piece p: tray)
			setShown(p, true);
	}
	
	public void hideAll()
	{
		for (int i=0;i<10;i++)
		for (int j=0;j<10;j++)
			if (grid[i][j] != null)
				setShown(grid[i][j], false);

		hideTray();
	}
	
	public void hideTray()
	{
		for (Piece p: tray)
			setShown(p, false);
	}
	
	protected void setKnown(Piece p, boolean b)
	{
		p.setKnown(b);
	}
	
	protected void setPiece(Piece p, Spot s)
	{
		grid[s.getX()][s.getY()] = p;
	}
	
	protected void setShown(Piece p, boolean b)
	{
		p.setShown(b);
	}
	
	public boolean move(Move m)
	{
		if (validMove(m))
		{
			recentMoves.add(m);
			if (recentMoves.size() > 4)
				recentMoves.remove(0);
			
			setPiece(m.getPiece(), m.getTo());
			setPiece(null, m.getFrom());
			return true;
		}
		
		return false;
	}
	
	public boolean remove(Spot s)
	{
		if (getPiece(s) == null)
			return false;
		if (getPiece(s) == INVALID)
			return false;
		
		setShown(getPiece(s), true);
		tray.add(getPiece(s));
		setPiece(null, s);
		Collections.sort(tray);
		return true;
	}
	
	private void scoutLose(Move m)
	{
		Spot tmp = getScoutLooseFrom(m);

		remove(m.getFrom());
		setPiece(getPiece(m.getTo()), tmp);
		setPiece(null, m.getTo());
	}
	
	private Spot getScoutLooseFrom(Move m)
	{
		if (m.getFrom().getX() == m.getTo().getX())
		{
			if (m.getFrom().getY() < m.getTo().getY())
				return new Spot(m.getTo().getX(), m.getTo().getY() - 1);
			else
				return new Spot(m.getTo().getX(), m.getTo().getY() + 1);
		}
		else //if (m.getFrom().getY() == m.getTo().getY())
		{
			if (m.getFrom().getX() < m.getTo().getX())
				return new Spot(m.getTo().getX() - 1, m.getTo().getY());
			else
				return new Spot(m.getTo().getX() + 1, m.getTo().getY());
		}
	}
	
	protected boolean validAttack(Move m)
	{
		if (m.getTo() != IN_TRAY)
			if (m.getTo().getX()<0||m.getTo().getX()>9||
				m.getTo().getY()<0||m.getTo().getY()>9)
				return false;
		if (getPiece(m.getTo()) == null)
			return false;
		if (getPiece(m.getFrom()) == null)
			return false;
		if (getPiece(m.getTo()).getRank().equals(Rank.WATER))
			return false;
		if (getPiece(m.getFrom()) != m.getPiece())
			return false;
		if (m.getPiece().getColor() == getPiece(m.getTo()).getColor())
			return false;
		

		Piece tmp = getPiece(m.getTo());
		setPiece(null, m.getTo());
		
		boolean valid = validMove(m);
		setPiece(tmp, m.getTo());
		
		return valid;

	}
	
	protected boolean validMove(Move m)
	{
		if (m.getTo() != IN_TRAY)
			if (m.getTo().getX()<0||m.getTo().getX()>9||
				m.getTo().getY()<0||m.getTo().getY()>9)
				return false;
		if (getPiece(m.getTo()) != null)
			return false;
		if (getPiece(m.getFrom()) == null)
			return false;
		if (getPiece(m.getFrom()) != m.getPiece())
			return false;
		
		//check for rule: "a player may not move their piece back and fourth.." or something
		if (recentMoves.contains(m))
			return false;

		switch (m.getPiece().getRank())
		{
		case FLAG:
		case BOMB:
			return false;
		}

		if (m.getFrom().getX() == m.getTo().getX())
			if (Math.abs(m.getFrom().getY() - m.getTo().getY()) == 1)
				return true;
		if (m.getFrom().getY() == m.getTo().getY())
			if (Math.abs(m.getFrom().getX() - m.getTo().getX()) == 1)
				return true;

		if (m.getPiece().getRank().equals(Rank.NINE))
			return validScoutMove(m.getFrom().getX(), m.getFrom().getY(), m.getTo(), m.getPiece());

		return false;
	}
	
	private boolean validScoutMove(int x, int y, Spot to, Piece p)
	{
		if ( !(grid[x][y] == null || p.equals(grid[x][y])) )
			return false;

		if (x == to.getX())
		{
			if (Math.abs(y - to.getY()) == 1)
			{
				p.setKnown(true); //scouts reveal themselves by moving more than one piece
				return true;
			}
			if (y - to.getY() > 0)
				return validScoutMove(x, y - 1, to, p);
			if (y - to.getY() < 0)
				return validScoutMove(x, y + 1, to, p);
		}
		else if (y == to.getY())
		{
			if (Math.abs(x - to.getX()) == 1)
			{
				p.setKnown(true);
				return true;
			}
			if (x - to.getX() > 0)
				return validScoutMove(x - 1, y, to, p);
			if (x - to.getX() < 0)
				return validScoutMove(x + 1, y, to, p);
		}

		return false;
	}

	private int winFight(Piece attack, Piece defend)
	{
		if (attack.getRank().equals(defend.getRank()))
		{
			if (Settings.bDefendAdvantage)
				return 0;
			else
				return -1;
		}
		if (defend.getRank().equals(Rank.FLAG))
			return 1;
		if (defend.getRank().equals(Rank.SPY))
			return 1;
		if (defend.getRank().equals(Rank.BOMB))
		{
			if (attack.getRank().equals(Rank.EIGHT))
				return 1;
			if (Settings.bOneTimeBombs)
				return -1;
			else
				return 0;
		}
		if (attack.getRank().equals(Rank.SPY))
		{
			if (defend.getRank().equals(Rank.ONE))
				return 1;
			return 0;
		}
		
		if (attack.getRank().toInt() < defend.getRank().toInt())
			return 1;
		return 0;
	}
}