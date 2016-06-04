package com.github.kdvolder.tttree.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.github.kdvolder.tttree.TTTree;
import com.github.kdvolder.util.ExceptionUtil;

/**
 * To play with the 2-3 tree interactively.
 */
public class InteractiveTest {

	public static void main(String[] args) throws IOException {
		TTTree<Integer, String> tree = TTTree.empty();

		p("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		p("!!!! TTTree interactive test!!!!!");
		p("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		p();
		p("A TTTree<Integer,String> is being maintained and modified by interactive commands.");
		p();
		p("Commands are:");
		p();
		p("  <Integer>=<some text>");
		p("    insert or overwrtie a value into the tree");
		p("  <Integer>");
		p("    remove any existing association for <Integer> from the tree");
		p("  exit");
		p("    end this program");

		boolean exit = false;
		while (!exit) {
			try {
				p("=============================");
				tree.dump();
				p("-----------------------------");
				String command = readline();
				if (command.equals("exit")) {
					exit = true;
				} else {
					int assign = command.indexOf("=");
					if (assign>=0) {
						int key = Integer.valueOf(command.substring(0, assign));
						String value = command.substring(assign+1);
						p("INSERT "+key+" -> "+value);
						tree = tree.put(key, value);
					} else {
						int key = Integer.valueOf(command);
						p("DELETE "+key);
						tree = tree.remove(key);
					}
					//TODO: maintain this invariant to save memory
					//TTTreeTest.checkForRedundantInternalKeys(tree);
				}
			} catch (Exception e) {
				p("ERROR: "+ExceptionUtil.getMessage(e));
			}
		}
	}

	private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

	private static String readline() throws IOException {
		System.out.print("command> ");
		return input.readLine().trim();
	}

	private static void p() {
		System.out.println();
	}

	private static void p(String string) {
		System.out.println(string);
	}

}
