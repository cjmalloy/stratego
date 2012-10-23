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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

import com.cjmalloy.stratego.Board;
import com.cjmalloy.stratego.Move;
import com.cjmalloy.stratego.Piece;
import com.cjmalloy.stratego.Rank;
import com.cjmalloy.stratego.Settings;
import com.cjmalloy.stratego.Spot;




public class AI implements Runnable
{
	private static final double ALPHA = 1;
	private static final int MAX_GUESSES = 3;
	public static ReentrantLock aiLock = new ReentrantLock();
	
	private Board board = null;
	private CompControls engine = null;

	private ReentrantLock tLock = new ReentrantLock();
	private Semaphore bLock = new Semaphore(1);
	private static int value = -1;
	private static Move move = null;
	private static TestingBoard tmpB = null;
	private static Move tmpM = null;
	private int turnF = 0;
	

	public AI(Board b, CompControls u) 
	{
		board = b;
		engine = u;
	}
	
	public void getMove() 
	{
		new Thread(this).start();
	}
	
	public void getBoardSetup() throws IOException
	{
		File f = new File("ai.cfg");
	    if(!f.exists()) f.createNewFile();
		BufferedReader cfg = new BufferedReader(new FileReader(f));
		ArrayList<String> setup = new ArrayList<String>();

		String fn;
		while ((fn = cfg.readLine()) != null)
			if (!fn.equals("")) setup.add(fn);
		
		while (setup.size() != 0)
		{
			Random rnd = new Random();
			fn = setup.get(rnd.nextInt(setup.size()));
			rnd = null;
			
			BufferedReader in;
			try
			{
				in = new BufferedReader(new FileReader(fn));
			}
			catch (Exception e)
			{
				setup.remove(fn);
				continue;
			}
			
			try
			{
				for (int j=0;j<40;j++)
				{
					int x = in.read(),
						y = in.read();

					if (x<0||x>9||y<0||y>3)
						throw new Exception();
					
					for (int k=0;k<board.getTraySize();k++)
						if (board.getTrayPiece(k).getColor() == Settings.topColor)
						{
							engine.aiReturnPlace(board.getTrayPiece(k), new Spot(x, y));
							break;
						}
				}
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "File Format Error: Unexpected end of file.", 
						"AI", JOptionPane.INFORMATION_MESSAGE);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, "File Format Error: Invalid File Structure.", 
						"AI", JOptionPane.INFORMATION_MESSAGE);
			}
			finally
			{
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		
		//double check the ai setup
		for (int i=0;i<10;i++)
		for (int j=0;j<4; j++)
		{
			Piece p = null;
			for (int k=0;k<board.getTraySize();k++)
				if (board.getTrayPiece(k).getColor() == Settings.topColor)
				{
					p = board.getTrayPiece(k);
					break;
				}

			if (p==null)
				break;
				
			engine.aiReturnPlace(p, new Spot(i, j));
		}
		
		//if the user didn't finish placing pieces just put them on
		for (int i=0;i<10;i++)
		for (int j=6;j<10;j++)
		{
			Piece p = null;
			for (int k=0;k<board.getTraySize();k++)
				if (board.getTrayPiece(k).getColor() != Settings.topColor)
				{
					p = board.getTrayPiece(k);
					break;
				}

			if (p==null)
				break;
				
			engine.aiReturnPlace(p, new Spot(i, j));
		}
		
		engine.play();
	}

	public void run() 
	{
		Move bestMove = null;
		
		aiLock.lock();
		try
		{
			TestingBoard c = new TestingBoard(board);
			bestMove = getBestMove(c, Settings.aiLevel);
		}
		finally
		{
			aiLock.unlock();
		}
		
		move = null;
		tmpB = null;
		tmpM = null;
		System.runFinalization();
		System.gc(); 
		
		engine.aiReturnMove(bestMove);
	}
	
	private Move getBestMove(TestingBoard b, int n)
	{
		final int n1 = n-1;
		
		if (n%2==0)
			turnF = 0;
		else
			turnF = 1;

		value = Rank.aiTotalValue() * -3;
		
		Thread threads[] = new Thread[160];
		int threadc = 0;
		
		for (int i=0;i<10;i++)
		for (int j=0;j<10;j++)
		{
			Spot f = new Spot(i, j);
			Spot t = null;
			if (b.getPiece(f) == null)
				continue;
			if (b.getPiece(f).getColor() != Settings.topColor)
				continue;
				
			for (int k=0;k<4;k++)
			{
				switch (k)
				{
				case 0:
					if (i==0)
						continue;
					t = new Spot(i-1, j);
					break;
				case 1:
					if (j==0)
						continue;
					t = new Spot(i, j-1);
					break;
				case 2:
					if (i==9)
						continue;
					t = new Spot(i+1, j);
					break;
				case 3:
					if (j==9)
						continue;
					t = new Spot(i, j+1);
					break;
				}
				if (b.validMove(f, t))
				{
					tmpM = new Move(b.getPiece(f), f, t);
					tmpB = new TestingBoard(b);
					tmpB.move(tmpM);
					threads[threadc] = new Thread()
					{
						public void run()
						{
							TestingBoard mytmpB = tmpB;
							Move mytmpM = tmpM;
							bLock.release();
							int tmpV = valueNMoves(mytmpB, n1, 0);
							tLock.lock();
							try
							{
								if (tmpV > value)
								{
									value = tmpV;
									move = mytmpM;
								}
							}
							finally
							{
								tLock.unlock();
							}
						}
					};
					bLock.acquireUninterruptibly();
					threads[threadc].start();
					threadc++;
					bLock.acquireUninterruptibly();
					bLock.release();
				}
			}
		}

		for (int i=0;i<160;i++)
			if (threads[i] != null)
				try
				{
					threads[i].join();
					threads[i] = null;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
		
		return move;
	}
	
	private int valueNMoves(TestingBoard b, int n, int guesses)
	{
		if (n<1)
			return valueBoard(b);

		boolean hasMove = false;
		
		int turn;
		if ((turnF + n) % 2 == 0)
			turn = Settings.topColor;
		else
			turn = Settings.bottomColor;
				
			
		int value = Rank.aiTotalValue();
		if (turn==Settings.topColor)
			value *= -3;
		
		for (int i=0;i<10;i++)
		for (int j=0;j<10;j++)
		{
			Spot f = new Spot(i, j);
			Spot t = null;
			if (b.getPiece(f) == null)
				continue;
			if (b.getPiece(f).getColor() != turn)
				continue;
				
			for (int k=0;k<4;k++)
			{
				switch (k)
				{
				case 0:
					if (j==9)
						continue;
					t = new Spot(i, j+1);
					break;
				case 1:
					if (i==9)
						continue;
					t = new Spot(i+1, j);
					break;
				case 2:
					if (i==0)
						continue;
					t = new Spot(i-1, j);
					break;
				case 3:
					if (j==0)
						continue;
					t = new Spot(i, j-1);
					break;
				}
				if (b.validMove(f, t))
				{
					hasMove = true;
					
					Move tmpM = new Move(b.getPiece(f), f, t);
					Piece fp = b.getPiece(f);
					Piece tp = b.getPiece(t);
					b.move(tmpM);
					//int tmpV = valueBoard(b) + (int)(valueNMoves(b, n-1) * ALPHA);
					int tmpV = valueBoard(b);
					if (tp == null ||
						(fp.isKnown() && turn == Settings.bottomColor) ||
						(tp.isKnown() && turn == Settings.topColor))
						tmpV += valueNMoves(b, n-1, guesses);
					else if (guesses <= MAX_GUESSES)
						tmpV += (int) (ALPHA * valueNMoves(b, n-1, guesses+1));
						
					b.undo(fp, f, tp, t);
					if (tmpV > value && turn == Settings.topColor)
					{
						value = tmpV;
						if (value > Rank.FLAG.aiValue()/2)
							//trim branches
							return value;
					}
					if (tmpV < value && turn == Settings.bottomColor)
					{
						value = tmpV;
						if (value < (Rank.FLAG.aiValue() * -1)/2)
							//trim branches
							return value;
					}
				}
			}
		}
		if (hasMove)
			return value;
		else
		{
			if (turn == Settings.topColor)
				return Rank.aiTotalValue();
			else 
				return -Rank.aiTotalValue();
		}
	}
	
	private int valueBoard(TestingBoard b)
	{
		int value = 0;
		
		for (int i=0;i<b.getTraySize();i++)
		{
			if (b.getTrayPiece(i).getColor() == Settings.bottomColor)
				value += b.getTrayPiece(i).getRank().aiValue();	
			else
				value -= b.getTrayPiece(i).getRank().aiValue();
		}
		
		return value;
	}
}
