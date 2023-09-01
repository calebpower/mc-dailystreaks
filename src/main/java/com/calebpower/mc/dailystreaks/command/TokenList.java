/*
 * Copyright (c) 2023 Caleb L. Power. All rights reserved.
 */
package com.calebpower.mc.dailystreaks.command;

import java.util.ArrayList;

/**
 * An argument tokenizer that provides for more dynamic token
 * grouping, provided as a list for easy access.
 * 
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class TokenList extends ArrayList<String> {
  private static final long serialVersionUID = 2130207879072355264L;

  /**
   * Instantiates this {@link TokenList} object.
   * 
   * @param args the raw array of arguments provided by Bukkit/Spigot
   * @param offset the index to start at (to throw away unnecessary args)
   */
  public TokenList(String[] args, int offset) {
    super();
    StringBuilder token = new StringBuilder();
    boolean escape = false;
    boolean quoted = false;
    for(int i = offset; i < args.length; i++) { // for each argument in the raw array
      for(int j = 0; j < args[i].length(); j++) { // for each character in the argument
        char c = args[i].charAt(j);
        if(c == '\\' && !escape) { // flag this as a character escape
          escape = true;
        } else if(c == '\"' && !escape) { // flag this as an open quote
          escape = false;
          quoted = !quoted; // toggle quote flag
          flush(token);
        } else { // otherwise treat as a standard character
          escape = false;
          token.append(c);
        }
      }
      if(!quoted || i == args.length - 1) // flush the token if it's the last one or if it's not quoted
        flush(token);
      else token.append(' '); // add a space between arguments
    }
  }
  
  /**
   * Instantiates this {@link TokenList} object, consuming the entire raw array.
   * 
   * @param args the raw array of arguments provided by Bukkit/Spigot
   */
  public TokenList(String[] args) {
    this(args, 0);
  }
  
  @Override public String get(int i) {
    // provide for retrieval of elements from the right side
    return super.get(i >= 0 ? i : (size() + i));
  }
  
  /**
   * Flush the token builder into the list wrapped by this object.
   * 
   * @param token the token builder
   */
  private void flush(StringBuilder token) {
    if(token.isEmpty()) return;
    add(token.toString());
    token.setLength(0);
  }
  
}
