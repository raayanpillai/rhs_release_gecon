package gecon.mod.alpha.misc;

import gecon.mod.alpha.BankItem;
import gecon.mod.alpha.DateAndPrice;
import gecon.mod.alpha.MarketOrder;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;

public class DatabaseMethods {
	private static final String fileName = "E:\\gECON_db.accdb";
	private static final String connectioinURL = "jdbc:odbc:DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="+fileName;

	/**
	 * Converts an Minecraft item ID into a gECON item ID
	 * @param MCItemID the Minecraft ID of the item
	 * @return the gECON item ID of the item
	 */
	public static int MCItemIDToGECONItemID(String MCItemID) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT gECONItemID FROM Items WHERE MCItemID='"+MCItemID+"'");
			
			ResultSet rs = s.getResultSet();
			
			int gECONItemID = -1;
			
			while((rs!=null) && (rs.next())) {
				gECONItemID = rs.getInt(1);
			}
			
			s.close();
			conn.close();
			
			return gECONItemID;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	/**
	 * Sets the new calculated price for an item based on recent market transactions
	 * @param itemName the name of the item as it appears in Minecraft
	 * @return boolean true if successful
	 */
	public static boolean setNewItemPrice(String MCItemID) {
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			transactions = getLastTwentyTransactions(MCItemID);
			
			double sum = 0;
			for (int i = 0; i < transactions.size(); i++) {
				sum += transactions.get(i).getPrice();
			}
			double average = sum/transactions.size();
			double modifier = average - getDefaultPrice(MCItemID);
			
			s.execute("UPDATE Items SET PriceModifier="+modifier+" WHERE gECONItemID="+getGECONItemID(MCItemID));
			
			s.close();
			conn.close();
			
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * Gets the suggested price
	 * @param MCItemID
	 * @return
	 */
	public static double getCurrentSuggestedPrice(String MCItemID) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT DefaultPrice, PriceModifier FROM Items WHERE MCItemID='"+MCItemID+"'");
			
			ResultSet rs = s.getResultSet();
			
			double price = -1;
			
			while ((rs!=null) && (rs.next())) {
				price =  rs.getDouble(1) + rs.getDouble(2);
			}
			
			s.close();
			conn.close();
			
			return price;
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	/**
	 * Increments the quantity of the passed item name associated with the passed player
	 * @param playerName the String player name
	 * @param MCItemID the Minecraft ID of the item
	 * @return true if access to the database was successful
	 */
	public static boolean incrementItemInBank(String playerName, String MCItemID) {
		return addItemsIntoBankAccount(playerName, MCItemID, 1);
	}
	
	/**
	 * Decrements the quantity of the passed item name associated with the passed player
	 * @param playerName the String player name
	 * @param MCItemID the Minecraft ID of the item
	 * @return true if access to the database was successful
	 */
	public static boolean decrementItemInBank(String playerName, String MCItemID) {
		return addItemsIntoBankAccount(playerName, MCItemID, -1);
	}
	
	/**
	 * Adds coins to a player (this method accepts negative numbers)
	 * @param playerName the String player name
	 * @param quantity the quantity of coins to add (can be negative)
	 * @return true if access to the database was successful
	 */
	public static boolean addCoins(String playerName, int quantity) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
	
			int playerID = getPlayerID(playerName);
	
			s.execute("UPDATE Players SET Coins = Coins + "+quantity+" WHERE PlayerID="+playerID);
		
			s.close();
			conn.close();
		
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns the player ID associated with the passed player name
	 * @param playerName the String player name
	 * @return the integer player ID
	 */
	public static int getPlayerID(String playerName) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT PlayerID FROM Players WHERE Username='"+playerName+"'");
		
			ResultSet rs = s.getResultSet();
			
			int playerID = -1;
			
			while((rs!=null) && (rs.next())) {
				playerID = rs.getInt(1);
			}
			 s.close();
		 	 conn.close();
			return playerID;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	/**
	 * Returns the player name associated with the passed player ID
	 * @param playerName the String player name
	 * @return the integer player ID
	 */
	public static String getPlayerName(int ID) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT Username FROM Players WHERE PlayerID="+ID);
		
			ResultSet rs = s.getResultSet();
			
			String username = "";
			
			while((rs!=null) && (rs.next())) {
				username = rs.getString(1);
			}
			 s.close();
		 	 conn.close();
			return username;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}
	/**
	 * Returns the number of a specific item stored in all banks
	 * @param MCItemID the Minecraft ID of the item
	 * @return the number of a specific item stored in all banks
	 */
	public static int getTotalNumItemsInBanks(String MCItemID) {
		try {
			String connURL = connectioinURL;
 			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			int gECONItemID = MCItemIDToGECONItemID(MCItemID);
			
			s.execute("SELECT Quantity FROM BankAccounts WHERE gECONItemID="+ gECONItemID);
	        
			ResultSet rs = s.getResultSet();
			
			int sum = 0;
				
			while((rs!=null) && (rs.next())) {
				sum += rs.getInt(1);
			}
					
			s.close();
			conn.close();
					
			return sum;
		} catch(Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Gets the integer item gECON ID of an item
	 * @param MCItemID the Minecraft ID of the item
	 * @return int gECONItemID
	 */
	public static int getGECONItemID(String MCItemID) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT gECONItemID FROM Items WHERE MCItemID='"+MCItemID+"'");
			
			ResultSet rs = s.getResultSet();
			
			int ID = -1;
			
			while((rs!=null) && (rs.next())) {
				ID = rs.getInt(1);
			}
			
			s.close();
			conn.close();
			
			return ID;
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Returns the Minecraft item ID using its GeconID
	 * @param itemName the String name for the item as it appears in Minecraft
	 * @return the Minecraft item ID
	 */
	public static String getMinecraftItemID(int geconID) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT MCItemID FROM Items WHERE gECONItemID="+geconID);
			
			ResultSet rs = s.getResultSet();
			
			String MCItemID = "0";
			
			while((rs!=null) && (rs.next())) {
				MCItemID = rs.getString(1);
			}
			
			s.close();
			conn.close();
			
			return MCItemID;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "0";
		}
	}
	
	/**
	 * Returns the String name of an item using a Minecraft item ID as a parameter
	 * @param MCItemID the Minecraft item ID of the item
	 * @return the String name of the item as it appears in Minecraft
	 */
	public static String getItemNameFromMCID(String MCItemID) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
		
			s.execute("SELECT ItemName FROM Items WHERE MCItemID='"+MCItemID+"'");
			
			ResultSet rs = s.getResultSet();
			
			String name = "";
			
			while((rs!=null) && (rs.next())) {
				name = rs.getString(1);
			}
			
			s.close();
			conn.close();
			
			return name;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Returns an ArrayList of BankItems connected to the passed player name
	 * @param playerName the String name of the player using the bank
	 * @return an ArrayList of BankItems connected to the passed player name
	 */
	public static ArrayList<BankItem> getBankItems(String playerName) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			int playerID = getPlayerID(playerName);
			s.execute("SELECT gECONItemID, Quantity FROM BankAccounts WHERE PlayerID="+playerID);
			
			ResultSet rs = s.getResultSet();
			
			ArrayList<BankItem> items = new ArrayList<BankItem>();
			
			while((rs!=null) && (rs.next())) {
				String out = getMinecraftItemID(Integer.parseInt(rs.getString(1)));
				int Quantity = rs.getInt(2);
				try {
					BankItem created = new BankItem(new ItemStack(Integer.parseInt(out), 0, 0));
					created.size += Quantity;
					items.add(created);
				} catch (Exception E) {
					int col = out.indexOf(":");
					int meta = Integer.parseInt(out.substring(col + 1));
					int id = Integer.parseInt(out.substring(0, col));
					BankItem created = new BankItem(new ItemStack(id, 0, meta));
					created.size += Quantity;
					items.add(created);
				}
			}
			 s.close();
		 	 conn.close();
			return items;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns an ArrayList of BankItems including every item in the game that applies to the economy (unit size > 0)
	 * @return an ArrayList of BankItems including every item in the game
	 */
	public static ArrayList<BankItem> getAllItems() {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT MCItemID FROM Items WHERE UnitSize > 0");
			
			ResultSet rs = s.getResultSet();
			
			ArrayList<BankItem> items = new ArrayList<BankItem>();
			
			while((rs!=null) && (rs.next())) {
				String out = rs.getString(1);
				try{
					BankItem created = new BankItem(new ItemStack(Integer.parseInt(out), 1, 0));
					items.add(created);

				}catch (Exception E){
					int col = out.indexOf(":");
					int meta = Integer.parseInt(out.substring(col + 1));
					int id = Integer.parseInt(out.substring(0, col));
					BankItem created = new BankItem(new ItemStack(id, 1, meta));
					items.add(created);
				}
			}
			
			s.close();
			conn.close();
			
			return items;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the String name of a specific item
	 * @param mcItemID mcItemID the minecraft ID of the item
	 * @return the String name of a specific item
	 */
	public static String getItemNameFromGECONID(int gECONItemID) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			s.execute("SELECT ItemName FROM Items WHERE gECONItemID="+gECONItemID);
			
			ResultSet rs = s.getResultSet();
			
			String name = "";
			
			while((rs!=null) && (rs.next())) {
				name = rs.getString(1);
			}
			
			s.close();
			conn.close();
			
			return name;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets the quantity of an item stored in the bank assosiated with a player
	 * @param MCItemID the Minecraft ID of the item
	 * @param itemName the name of the item as it appears in Minecraft
	 * @return
	 */
	public static int getNumItemsInBank(String playerName, String MCItemID) {
		try {
			String connURL = connectioinURL;
 			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			int playerID = getPlayerID(playerName);
			int gECONItemID = getGECONItemID(MCItemID);
			s.execute("SELECT Quantity FROM BankAccounts WHERE (gECONItemID="+gECONItemID+" AND playerID="+playerID+")");
	        
			ResultSet rs = s.getResultSet();
			
			int sum = 0;
			
			while((rs!=null) && (rs.next())) {
				sum += rs.getInt(1);
			}
					
			s.close();
			conn.close();
					
			return sum;
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	/**
	 * Checks if the player account is there, if not it makes one.
	 * @param playerName
	 * @return
	 */
	public static boolean hasPlayerAccount(String playerName){
		if(getPlayerID(playerName) != -1)
			return true;
		else{
			try {
				String connURL = connectioinURL;
				Connection conn = DriverManager.getConnection(connURL, "","");
				Statement s = conn.createStatement();
				s.execute("INSERT INTO Players (Username, Coins) VALUES ('"+playerName+"', "+0+")");
				
				s.close();
				conn.close();
				
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * Returns the amount of coins assosiated with a player
	 * @param playerName the String player name
	 * @return the integer number of coins a player has
	 */
	public static int getCoins(String playerName) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
		
			int playerID = getPlayerID(playerName);
		
			s.execute("SELECT Coins FROM Players WHERE PlayerID="+playerID);
		
			ResultSet rs = s.getResultSet();
		
			int coins = 0;
		
			while((rs!=null) && (rs.next())) {
				coins = rs.getInt(1);
			}
			
			s.close();
			conn.close();
			
			return coins;
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Returns the item price of an item
	 * @param MCItemID the Minecraft ID of the item
	 * @return double the price of an item
	 */
	public static double getItemPrice(String MCItemID) {
		return 0;
	}
	/**
	 * Returns the gECON suggestion of a specific item
	 * @param itemName the name of the item as it appears in Minecraft
	 * @return the gECON suggestion of a specific item
	 */
	public static String getGECONSuggestion(String MCItemID) {
		return "";
	}
	/**
	 * Get Default Price
	 * @param MCItemID
	 * @return
	 */
	public static double getDefaultPrice(String MCItemID) {
		if(!MCItemID.equals("266")){
		 try {
		 	String connURL = connectioinURL;
		 	Connection conn = DriverManager.getConnection(connURL, "","");
		 	Statement s = conn.createStatement();
		 	 
		 	s.execute("SELECT DefaultPrice FROM Items WHERE MCItemID='"+MCItemID+"'");

		 	ResultSet rs = s.getResultSet();
		 	 
		 	double defaultPrice = -1.0;

		 	while((rs!=null) && (rs.next())) {
		 	 defaultPrice = rs.getDouble(1);
		 	}

		 	s.close();
		 	conn.close();

		 	return defaultPrice;
		 } catch (Exception ex) {
		 	ex.printStackTrace();
		 }
		 return -1;
		}else
			 return 200;
	}
	/**
	 * Adds a quantity of items into a bank account.  Deletes entry if the quantity pulls the bank quantity to 0.  Adds a new entry if there isn't an existing one for the player and item.
	 * @param playerName the String name of the player 
	 * @param MCItemID the Minecraft ID of the item
	 * @param quantity the quantity
	 * @return true if access to the database was successful
	 */
	public static boolean addItemsIntoBankAccount(String playerName, String MCItemID, int quantity) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			int playerID = getPlayerID(playerName);
			int gECONItemID = MCItemIDToGECONItemID(MCItemID);
			
			int bankQuantity = getNumItemsInBank(playerName, MCItemID);
			if (quantity < 0) {				
				if ((bankQuantity + quantity) == 0) {
					s.execute("DELETE FROM BankAccounts WHERE (PlayerID="+playerID+" AND gECONItemID="+gECONItemID+")");
				} else {
					s.execute("UPDATE BankAccounts SET Quantity = Quantity + "+quantity+ " WHERE (playerID="+playerID+" AND gECONItemID="+gECONItemID+")");
				}
			} else {
				if (hasEntry(playerName, MCItemID)) {
					s.execute("UPDATE BankAccounts SET Quantity = Quantity + "+quantity+ " WHERE (playerID="+playerID+" AND gECONItemID="+gECONItemID+")");
				} else {
					addNewBankEntry(playerName, MCItemID, quantity);
				}
			}
			
			s.close();
			conn.close();
			
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Adds a new entry into BankAccounts.  Used when there is no current entry for an item.
	 * @param playerName the String name of the player 
	 * @param MCItemID the Minecraft ID of the item
	 * @param quantity the quantity
	 * @return
	 */
	public static boolean addNewBankEntry(String playerName, String MCItemID, int quantity) {
		try {
			String connURL = connectioinURL;
			Connection conn = DriverManager.getConnection(connURL, "","");
			Statement s = conn.createStatement();
			
			int playerID = getPlayerID(playerName);
			int gECONItemID = MCItemIDToGECONItemID(MCItemID);
			
			s.execute("INSERT INTO BankAccounts VALUES ("+playerID+", "+gECONItemID+", "+quantity+")");
			
			s.close();
			conn.close();
			
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns true if there is an existing entry for the player and item
	 * @param playerName the String name of the player 
	 * @param MCItemID the Minecraft ID of the item
	 * @return
	 */
	public static boolean hasEntry(String playerName, String MCItemID) {
		try {		
			int numItems = getNumItemsInBank(playerName, MCItemID);
			if (numItems > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
	}
	/**
	  * Returns the summed quantity for all sell orders all for one item
	  * @param MCItemID
	  * @return
	 */
	 public static int getTotalQuantityForSale(String MCItemID) {
	 	try {
	 	 String connURL = connectioinURL;
	 	 Connection conn = DriverManager.getConnection(connURL, "","");
	 	 Statement s = conn.createStatement();
	 	  
	 	 int gECONItemID = MCItemIDToGECONItemID(MCItemID);
	 	  
	 	 s.execute("SELECT Quantity FROM SellOrders WHERE gECONItemID="+gECONItemID);
	 	  
	 	 ResultSet rs = s.getResultSet();
	 	  
	 	 int sum = 0;
	 	  
	 	 while ((rs!=null) && (rs.next())) {
	 	 sum += rs.getInt(1);
	 	 }
	 	 s.close();
	 	 conn.close();
	 	 return sum;
	 	  
	 	} catch (Exception ex) {
	 	 ex.printStackTrace();
	 	 return -1;
	 	 } 
	 }
	  
	 /**
	  * Returns the summed quantity for all buy orders all for one item
	  * @param MCItemID
	  * @return
	  */
	 public static int getTotalQuantityForBuy(String MCItemID) {
	 	try {
	 	 String connURL = connectioinURL;
	 	 Connection conn = DriverManager.getConnection(connURL, "","");
	 	 Statement s = conn.createStatement();
	 	  
	 	 int gECONItemID = MCItemIDToGECONItemID(MCItemID);
	 	  
	 	 s.execute("SELECT Quantity FROM BuyOrders WHERE gECONItemID="+gECONItemID);
	 	  
	 	 ResultSet rs = s.getResultSet();
	 	  
	 	 int sum = 0;
	 	  
	 	 while ((rs!=null) && (rs.next())) {
	 	 sum += rs.getInt(1);
	 	 }
	 	 s.close();
	 	 conn.close();
	 	 return sum;
	 	  
	 	} catch (Exception ex) {
	 	 ex.printStackTrace();
	 	 return -1;
	 	}
	 }
	  
	 /**
	  * Returns all the Buy Orders
	  * @param MCItemID
	  * @return
	  */
	 public static ArrayList<MarketOrder> getTotalBuyOrders(String MCItemID) {
	 	try {
	 	 String connURL = connectioinURL;
	 	 Connection conn = DriverManager.getConnection(connURL, "","");
	 	 Statement s = conn.createStatement();
	 	  
	 	 int gECONItemID = MCItemIDToGECONItemID(MCItemID);
	 	  
	 	 s.execute("SELECT BuyOrders.PlayerID, BuyOrders.Quantity, BuyOrders.MaxPrice, BuyOrders.OpenDate, BuyOrders.BuyOrderID FROM BuyOrders WHERE (((BuyOrders.OrderFilled)=False) AND ((BuyOrders.gECONItemID)=" + gECONItemID + ")) ORDER BY BuyOrders.MaxPrice");
	 	  
	 	 ResultSet rs = s.getResultSet();
	 	  
	 	 ArrayList<MarketOrder> orders = new ArrayList<MarketOrder>();
	 	 
	 	 while ((rs!=null) && (rs.next())) {
	 		MarketOrder created = new MarketOrder(MCItemID, rs.getInt(2), rs.getDouble(3), getPlayerName(rs.getInt(1)), true, rs.getDate(4), rs.getInt(5));
	 		orders.add(created);
	 	 }
	 	 s.close();
	 	 conn.close();
	 	 return orders;

	 	} catch (Exception ex) {
	 	 ex.printStackTrace();
	 	 return null;
	 	}
	 }
	 
	 /**
	  * Returns all the Sell Orders
	  * @param MCItemID
	  * @return
	  */
	 public static ArrayList<MarketOrder> getTotalSellOrders(String MCItemID) {
	 	try {
	 	 String connURL = connectioinURL;
	 	 Connection conn = DriverManager.getConnection(connURL, "","");
	 	 Statement s = conn.createStatement();
	 	  
	 	 int gECONItemID = MCItemIDToGECONItemID(MCItemID);
	 	  
	 	 s.execute("SELECT SellOrders.PlayerID, SellOrders.Quantity, SellOrders.MinPrice, SellOrders.OpenDate, SellOrders.SellOrderID FROM SellOrders WHERE (((SellOrders.OrderFilled)=False) AND ((SellOrders.gECONItemID)=" + gECONItemID + ")) ORDER BY SellOrders.MinPrice");
	 	  
	 	 ResultSet rs = s.getResultSet();
	 	  
	 	 ArrayList<MarketOrder> orders = new ArrayList<MarketOrder>();
	 	 
	 	 while ((rs!=null) && (rs.next())) {
	 		MarketOrder created = new MarketOrder(MCItemID, rs.getInt(2), rs.getDouble(3), getPlayerName(rs.getInt(1)), false, rs.getDate(4), rs.getInt(5));
	 		orders.add(created);
	 	 }
	 	 s.close();
	 	 conn.close();
	 	 return orders;

	 	} catch (Exception ex) {
	 	 ex.printStackTrace();
	 	 return null;
	 	}
	 }
	 /**
	  * Returns an economy suggestion
	  * @param MCItemID
	  * @return
	  */
	 public static String getEconomySuggestion(String MCItemID) {
	 	int qfb = getTotalQuantityForBuy(MCItemID);
	 	int qfs = getTotalQuantityForSale(MCItemID);
	 	if (qfb > qfs) {
	 	 return "Needs Sellers";
	 	} else if (qfb < qfs){
	 	 return "Needs Buyers";
	 	}
	 	
	 	return "N/A";
	 	
	 }
	 /**
		 * Returns a two-dimensional array of Strings containing Dates and Prices of the last 10% of transactions
		 * @param itemName the name of the item as it appears in Minecraft
		 * @return a two-dimensional array of Strings containing Dates and Prices of the last 10% of transactions
		 */
		public static ArrayList<Transaction> getLastTwentyTransactions(String MCItemID) {
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			
			try {
				String connURL = connectioinURL;
				Connection conn = DriverManager.getConnection(connURL, "","");
				Statement s = conn.createStatement();
				
				int gECONItemID = MCItemIDToGECONItemID(MCItemID);
				
				s.execute("SELECT TOP 20 TransactionDate, TransactionPrice FROM Transactions WHERE gECONItemID="+gECONItemID);
				
				ResultSet rs = s.getResultSet();
				
				while((rs!=null) && (rs.next())) {
					Transaction trans = new Transaction(gECONItemID, rs.getDouble(2), rs.getDate(1));
					transactions.add(trans);
				}
				
				s.close();
				conn.close();

				return transactions;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		public static double getHighAndLow(String MCItemID, double base){
			ArrayList<Transaction> transactions = getLastTwentyTransactions(MCItemID);
			double max = Integer.MIN_VALUE;
			for(Transaction x: transactions){
				if(x.getPrice() > max)
					max = x.getPrice();
			}			
			max = (int)((max/base)*100);
			if(max < 200)
				max = 200;
			return max;
			
		}

		/**
		 * Records a transation into the database
		 * @param t the Transaction
		 * @return true if successful; false if failed
		 */
		public static boolean recordTransaction(Transaction t) {
			try {
				String connURL = connectioinURL;
				Connection conn = DriverManager.getConnection(connURL, "","");
				Statement s = conn.createStatement();
				
				
				int gECONItemID = t.getGECONItemID();
				//Modify transaction table to be these three columns (plus transaction ID)
				if(gECONItemID != 1){
					double price = t.getPrice();
					s.execute("INSERT INTO Transactions (gECONItemID, TransactionDate, TransactionPrice) VALUES ("+gECONItemID+", Date(), "+price+")");
				}
				setNewItemPrice(getMinecraftItemID(t.getGECONItemID()));
				s.close();
				conn.close();
				
				return true;
				
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		/**
		 * Create Sell Order
		 */
		public static boolean createSellOrder(String playerName, String MCItemID, int qty, double price) {
			try {
				String connURL = connectioinURL;
				Connection conn = DriverManager.getConnection(connURL, "","");
				Statement s = conn.createStatement();
				
				s.execute("INSERT INTO SellOrders (PlayerID, gECONItemID, Quantity, MinPrice, OpenDate) VALUES ("+ getPlayerID(playerName)+", "+getGECONItemID(MCItemID)+", " + qty +", "+price+", Date())");
				addItemsIntoBankAccount(playerName, MCItemID, -1*qty);
				
				s.close();
				conn.close();
				
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		/**
		 * Create Buy Order
		 */
		public static boolean createBuyOrder(String playerName, String MCItemID, int qty, double price) {
			try {
				String connURL = connectioinURL;
				Connection conn = DriverManager.getConnection(connURL, "","");
				Statement s = conn.createStatement();
				
				s.execute("INSERT INTO BuyOrders (PlayerID, gECONItemID, Quantity, MaxPrice, OpenDate) VALUES ("+ getPlayerID(playerName)+", "+getGECONItemID(MCItemID)+", " + qty +", "+price+", Date())");
				addCoins(playerName, (int)(-1*price*qty));
				
				s.close();
				conn.close();
				
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		/**
		 * Sets a completed order to filled
		 * @param mo the MarketOrder to be fufilled
		 * @param buying 
		 * @return true if sucessful; false if failed
		 */
		public static boolean setOrderFilled(MarketOrder mo) {
			try {
				String connURL = connectioinURL;
				Connection conn = DriverManager.getConnection(connURL, "","");
				Statement s = conn.createStatement();
				
				int orderID = mo.getOrderID();
				String orderType = mo.getOrderType();
				
				s.execute("UPDATE "+orderType+ "s SET OrderFilled=True WHERE "+orderType+"ID="+orderID);
				recordTransaction(new Transaction(mo.item.ID, mo.price, mo.date));
				
				
				s.close();
				conn.close();
				
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		/**
		 * Sets a order to be partially filled by updating the quantity
		 * @param mo the MarketOrder to be fufilled
		 * @param newQuantity the new quantity to be updated in the database
		 * @return true if sucessful; false if failed
		 */
		public static boolean setOrderPartiallyFilled(MarketOrder mo, int newQuantity) {
			try {
				String connURL = connectioinURL;
				Connection conn = DriverManager.getConnection(connURL, "","");
				Statement s = conn.createStatement();
				
				int orderID = mo.getOrderID();
				String orderType = mo.getOrderType();
				
				s.execute("UPDATE "+orderType+"s SET Quantity="+newQuantity+" WHERE "+orderType+"ID="+orderID);
				
				s.close();
				conn.close();
				
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
}

