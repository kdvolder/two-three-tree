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

Why not use Google Guava?
=========================

Google's wonderful Guava library provides `ImmutableMap` 
and `ImmutableSortedMap` implementations. So why not
just use that instead?

The answer is, it depends on what you wanted to use it for.

Guava's immutable sorted map is most similar to `TTTree`.
However, Guava's immutables seem to optimized towards a
compact representation of a *single* map instance. It's
ideal if you want to keep a map containing some 'static' 
data assigned to a constant. 

It also performs pretty well when you need to make identical
copies of this map (essentially an identical copy is free).

However, it performs rather poorly when you are interested 
in making modified copies of a map. Essentially making a
modified copy implies pretty much 'rebuilding' the map 
again with a small change to its contents. So its performance
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
   
 - access times are comparable both are log(n) but Guava's
   ImmutableSortedMap probably has an edge in raw access speed. I've done
   some test runs but   I'll probably need to devise a more rigourous comparison 
   as the one I threw together quickly, the time measurements are probably 
   a bit sloppy and 'noisy'. Some runs it looks like TTTree is faster and
   others it looks like Guava is the clear winner.
 
For whatever its worth the table below shows the time in seconds to
peform 1_000_000 'get' calls on a table of different sizes in the sloppy 
test code I have available now.

|    Size | TTTree |    Guava |
|--------:|-------:|---------:|
|       1 |  0.026 |    0.024 |
|      10 |  0.013 |    0.035 |
|     100 |  0.037 |    0.060 |
|    1000 |  0.098 |    0.112 |
|   10000 |  0.162 |    0.141 |

  