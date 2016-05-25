Two-Three Tree
==============

[![Build Status](https://travis-ci.org/kdvolder/two-three-tree.svg?branch=master)](https://travis-ci.org/kdvolder/two-three-tree)

What is this?
-------------

This is an implementation of a immutable key-value store (i.e. a 'Map').
The store is immutable. This means that adding, removing or
changing a key-value pair doesn't alter the map but instead 
creates a copy.

The implementation uses a 2-3-tree and is entirely built
out of immutable nodes. Creating a copy is implemented 
efficiently. Most of the original tree's nodes are re-used to 
build the copy. Roughly speaking the operation will end up 
making copies only of nodes along a path from the tree's root 
down to the inserted/deleted leaf. So the amount of copying 
is O(log(n)).

How to use it
-------------

Maven dependency:

		<dependency>
			<groupId>com.github.kdvolder</groupId>
			<artifactId>two-three-tree</artifactId>
			<version>0.0.1-RELEASE</version>
		</dependency>

Create an empty tree to get started:

		TTTree<String, String> tree = TTTree.empty();

Add entries using `.put()` method to create an extended copy of a map:

		largerTree = tree.put("Hello", "World!");
		
Lookup entries using `.get()`:

		System.out.println(tree.get("Hello"));
		
Remove entries using `.remove()` to create a reduced copy of a map: 

		smallerTree = tree.remove("Hello");
		
Why not use Google Guava ?
--------------------------

Google's Guava library is great. I love it. It 
provides `ImmutableMap` and `ImmutableSortedMap` 
implementations. 

So why not just use that instead?

The answer is, it depends on what you wanted to use it for.

Guava's immutable sorted map is most similar to `TTTree`.
So let's compare the two. Guava's immutables seem to optimized 
towards a compact representation of a *single* map instance. So 
it's ideal if all you want is to keep a map containing some 'static' 
data assigned to a constant forever. 

However, it performs rather poorly when you are interested 
in making *modified* copies of this map. Essentially making a
modified copy with an extra/removed key, implies 'rebuilding' 
the whole map with just a small change to its contents. So 
its performance is O(n) in both time and space. If you need to
do a lot of copying this becomes problematic.

In contrast, TTTree makes modified copy of a map in O(log(n))
time and space. This remains practical, even for larger maps.

For a quick performance comparison, I measured the time it takes
to build up a map, starting from a empty map and adding
one entry at a time, creating a modified copy each time. 

The table below shows the time taken for executing 1,000,000
inserts in this scenario to build up maps of different sizes.

|    Size | TTTree |    Guava |
|--------:|-------:|---------:|
|       1 |  0.029 |    0.064 |
|      10 |  0.065 |    0.313 |
|     100 |  0.168 |    1.576 |
|   1,000 |  0.263 |   14.173 |
|  10,000 |  0.380 |  140.890 |

The price you pay for this spectacular speedup is that a *single* 
TTTree will take significantly more memory than a single
`ImmutableSortedSet` containing the same data. For larger maps,
on a 64bit VM with compressed oops `ImmutableSortedSet` uses only
slightly over 8 bytes on average per entry. `TTTree` needs about 50 bytes
per entry.

What about access times? A bit surprising, but access times especially for 
smaller sized maps upto 10_000 entries are very comparable between Guava and 
TTTree. Starting at about 10_000 entries my tests showed that Guava is gaining 
a significant edge over TTTree in raw access speed (better cache 
locality due to its super-compact array-based representation?). Below 
10,000 entries access speeds where comparable or even better with TTTree.

The table below shows time taken in seconds, to perform 100,000,000 
`.get` calls on maps of different sizes.

|    Size | TTTree |    Guava |
|--------:|-------:|---------:|
|       1 |  0.358 |    0.595 |
|      10 |  1.514 |    2.445 |
|     100 |  2.381 |    3.091 |
|   1,000 |  8.031 |    8.103 |
|  10,000 | 14.725 |   11.449 |
| 100,000 | 31.194 |   18.412 |

