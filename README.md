Two-Three Tree
==============

[![Build Status](https://travis-ci.org/kdvolder/two-three-tree.svg?branch=master)](https://travis-ci.org/kdvolder/two-three-tree)

Implementation of a immutable key-value store.

The store is immutable. This means that adding/removing a 
key-value pair doesn't alter the store but instead creates a 
copy.

Because the store is based on a tree and is entirely built
out of immutable nodes, the copy can be made efficiently,
reusing most of the original tree's nodes to build the copy.
Roughly speaking the operation will end up making copies 
only of nodes along a path from the tree's root down to the
inserted/deleted leaf. So the amount of copying is O(log(n)).

Why not use Google Guava ?
==========================

Google's wonderful Guava library is great. I love it. It 
provides `ImmutableMap` and `ImmutableSortedMap` 
implementations. So why not just use that instead?

The answer is, it depends on what you wanted to use it for.

Guava's immutable sorted map is most similar to `TTTree`.
However, Guava's immutables seem to optimized towards a
compact representation of a *single* map instance. It's
ideal if you want to keep a map containing some 'static' 
data assigned to a constant. 

It also performs pretty well when you need to make identical
copies of this map (essentially an identical copy is free).

However, it performs rather poorly when you are interested 
in making *modified* copies of a map. Essentially making a
modified copy implies 'rebuilding' the whole map 
with a small change to its contents. So its performance
is O(n) in both time and space.

In contrast, TTTree makes modified copy of a map in O(log(n)).

For a quick performance comparison, I wrote some code that
builds up a map, starting from a empty map and adding
one entry at a time, (each time creating a modified copy). These
are the results tabulated by map size. The time shown are for
a million inserts performed (a loop is wrapped around the test
to repeat it more often for smaller tables so that each table-size
test performs the exact same number of total inserts overall)

|    Size | TTTree |    Guava |
|--------:|-------:|---------:|
|       1 |  0.029 |    0.064 |
|      10 |  0.065 |    0.313 |
|     100 |  0.168 |    1.576 |
|    1000 |  0.263 |   14.173 |
|   10000 |  0.380 |  140.890 |

The price you pay for this spectacular speedup is this:

 - a *single* TTTree will probably take up quite a bit more memory space 
   (I haven't gotten numbers on that yet). However the situation is 
   reversed if you need to keep both original map and modified versions of
   the map in memory at the same time.
   

What about access times? A bit surprising, but access times especially for 
smaller sized maps upto 10_000 entries are very comparable between Guava and 
TTTree. Upwards of that Guava seems to gain a significant edge (better 
cache locality due to its compact array-based representation?). 

The table below shows time taken in seconds, to perform 100,000,000 
`.get` calls on maps of different sizes.

|    Size | TTTree |    Guava |
|--------:|-------:|---------:|
|       1 |  0.358 |    0.595 |
|      10 |  1.514 |    2.445 |
|     100 |  2.381 |    3.091 |
|    1000 |  8.031 |    8.103 |
|   10000 | 14.725 |   11.449 |
|  100000 | 31.194 |   18.412 |

