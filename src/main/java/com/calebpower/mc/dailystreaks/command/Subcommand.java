package com.calebpower.mc.dailystreaks.command;

import java.util.Objects;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

public abstract class Subcommand implements Comparable<Subcommand> {

  private CommandPermission permission = null;
  private DailyStreaks plugin = null;
  private String description = null;
  private String argUsage = null;
  private String[][] subcommands = null;
  
  /**
   * Instates the subcommand.
   * 
   * @param plugin the {@ink DailyStreaks} instance
   * @param description a description of the subcommand
   * @param subcommands an array of args that together represent the string
   *        required to invoke the subcommand
   */
  protected Subcommand(DailyStreaks plugin, String description, String[] subcommands) {
    this(plugin, description, subcommands, "");
  }
  
  /**
   * Instantiates the subcommand.
   * 
   * @param plugin the {@link DailyStreaks} instance
   * @param description a description of the subcommand
   * @param subcommands an array of args that together represent the string
   *        required to invoke the subcommand
   * @param argUsage additional arguments that may be either required or optional
   *        for use by this subcommand
   */
  protected Subcommand(DailyStreaks plugin, String description, String[] subcommands, String argUsage) {
    this(plugin, description, CommandPermission.PLUGIN_ADMINISTRATOR, subcommands, argUsage);
  }
  
  /**
   * Instantiates the subcommand.
   * 
   * @param plugin the {@link DailyStreaks} instance
   * @param description a description of the subcommand
   * @param permission the level of authority required to execute the subcommand
   * @param subcommands an array of args that together represent the string
   *        required to invoke the subcommand
   */
  protected Subcommand(DailyStreaks plugin, String description, CommandPermission permission, String[] subcommands) {
    this(plugin, description, permission, subcommands, "");
  }
  
  /**
   * Instantiates the subcommand.
   * 
   * @param plugin the {@link DailyStreaks} instance
   * @param description a description of the subcommand
   * @param permission the level of authority required to execute the subcommand
   * @param subcommands an array of args that together represent the string
   *        required to invoke the subcommand
   * @param argUsage additional arguments that may be either required or optional
   *        for use by this command
   */
  protected Subcommand(DailyStreaks plugin, String description, CommandPermission permission, String[] subcommands, String argUsage) {
    this(plugin, description, permission, new String[][] { subcommands }, argUsage);
  }
  
  /**
   * Instantiates the command.
   * 
   * @param plugin the {@link DailyStreaks} instance
   * @param description a description of the command
   * @param subcommands a double array of args, with each primary array representing
   *        a string that can be used to invoke the subcommand
   */
  protected Subcommand(DailyStreaks plugin, String description, String[][] subcommands) {
    this(plugin, description, subcommands, "");
  }
  
  /**
   * Instantiates the command.
   * 
   * @param plugin the {@link DailyStreaks} instance
   * @param description a description of the command
   * @param subcommands a double array of args, with each primary array representing
   *        a string that can be used to invoke the subcommand
   * @param argUsage additional arguments that may either be required or optional
   *        for use by this command
   */
  protected Subcommand(DailyStreaks plugin, String description, String[][] subcommands, String argUsage) {
    this(plugin, description, CommandPermission.PLUGIN_ADMINISTRATOR, subcommands, argUsage);
  }
  
  /**
   * Instantiates the command.
   * 
   * @param plugin the {@link DailyStreaks} instance
   * @param description a description of the command
   * @param permission the level of authority required to execute the subcommand
   * @param subcommands a double array of args, with each primary array representing
   *        a string that can be used to invoke the subcommand
   */
  protected Subcommand(DailyStreaks plugin, String description, CommandPermission permission, String[][] subcommands) {
    this(plugin, description, permission, subcommands, "");
  }
  
  /**
   * Instantiates the command.
   * 
   * @param plugin the {@link DailyStreaks} instance
   * @param description a description of the command
   * @param permission the level of authority required to execute the subcommand
   * @param subcommands a double array of args, with each primary array representing
   *        a string that can be used to invoke the subcommand
   * @param argUsage additional arguments that may either be required or optional
   *        for use by this command
   */
  protected Subcommand(DailyStreaks plugin, String description, CommandPermission permission, String[][] subcommands, String argUsage) {
    this.plugin = plugin;
    this.description = description;
    this.argUsage = argUsage;
    this.subcommands = subcommands;
    this.permission = permission;
  }
  
  /**
   * Retrieves the plugin for which this command acts on behalf of.
   * 
   * @return a {@link DailyStreaks} object
   */
  protected final DailyStreaks getPlugin() {
    return plugin;
  }
  
  /**
   * Retrieves the description associated with this subcommand.
   * 
   * @return a string representation of the subcommand's description
   */
  public final String getDescription() {
    return description;
  }
  
  /**
   * Retrieves a usage string denoting additional required and optional arguments. 
   * 
   * @return a string with command syntax representation
   */
  public final String getArgUsage() {
    return argUsage;
  }
  
  /**
   * A double string array, wherein each primary string array represents
   * a possible string to be used to invoke this particular subcommand.
   *  
   * @return a String[][] representing this subcommand's command strings
   */
  public final String[][] getSubcommands() {
    return subcommands;
  }
  
  /**
   * Determines whether or not the sender has permission to execute this subcommand.
   * 
   * @param sender the candidate whose authority is being put to the test
   * @return {@code true} iff the sender has the authority to execute this subcommand
   */
  public final boolean hasPermission(CommandSender sender) {
    return permission.hasPermission(sender);
  }

  /**
   * Executes the main workflow associated with this particular subcommand.
   * 
   * @param sender the {@link CommandSender} responsible for executing the subcommand
   * @param args a {@link TokenList} representing args send along with the subcommand
   * @throws SubcommandException if any error, sender-caused or otherwise, occurs
   *         through the due course of the workflow's execution
   */
  public abstract void onCommand(CommandSender sender, TokenList args) throws SubcommandException;

  @Override public boolean equals(Object object) {
    // command equality is primarily based on object type and the first set of invocation args
    if(null == object || !(object instanceof Subcommand)) return false;
    return 0 == compareTo((Subcommand)object);
  }
  
  @Override public int hashCode() {
    return subcommands[0].hashCode();
  }

  @Override public int compareTo(Subcommand command) {
    // this should primarily be used to order subcommands lexicographically
    Objects.requireNonNull(command);
    
    String[] s1 = subcommands[0];
    String[] s2 = command.subcommands[0];
    
    for(int i = 0; i < (s1.length < s2.length ? s1.length : s2.length); i++) {
      int comparison = s1[i].compareTo(s2[i]);
      if(0 != comparison) return comparison;
    }
    
    return Integer.compare(s1.length, s2.length);
  }

  /**
   * An exception to be thrown through the due course of a subcommand's execution.
   */
  public class SubcommandException extends Exception {
    private static final long serialVersionUID = 5673176169849925679L;
    
    private boolean displayHelp = false;
    
    /**
     * Instantiates the exception.
     * 
     * @param displayHelp {@code true} if the generic help message should be shown
     */
    protected SubcommandException(boolean displayHelp) {
      this.displayHelp = displayHelp;
    }

    /**
     * Instantiates the exception with a message.
     *
     * @param displayHelp {@code true} if the generic help message should be shown
     * @param message the error message that should be shown to the user
     */
    protected SubcommandException(boolean displayHelp, String message) {
      super(message);
      this.displayHelp = displayHelp;
    }
    
    /**
     * Determines whether or not the generic help message should be displayed.
     * 
     * @return {@code true} iff the generic help message should be displayed
     */
    public boolean doDisplayHelp() {
      return displayHelp;
    }
    
    /**
     * Retrieves the subcommand that ultimately threw this exception.
     * 
     * @return the {@link Subcommand} responsible for throwing this exception
     */
    public Subcommand getCommand() {
      return Subcommand.this;
    }
    
  }
  
}
