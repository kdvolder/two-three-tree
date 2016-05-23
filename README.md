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

Google's Guava library is great. I love it. It 
provides `ImmutableMap` and `ImmutableSortedMap` 
implementations. So why not just use that instead?

The answer is, it depends on what you wanted to use it for.

Guava's immutable sorted map is most similar to `TTTree`.
However, Guava's immutables seem to optimized towards a
compact representation of a *single* map instance. It's
ideal if you want to keep a map containing some 'static' 
data assigned to a constant. 

It also performs pretty well when you make identical
copies of this map. Essentially an identical copy is free.

However, it performs rather poorly when you are interested 
in making *modified* copies of a map. Essentially making a
modified copy implies 'rebuilding' the whole map 
with a small change to its contents. So its performance
is O(n) in both time and space.

In contrast, TTTree makes modified copy of a map in O(log(n))
time and space.

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

The price you pay for this spectacular speedup is this:

 - a *single* TTTree will probably take up quite a bit more memory space 
   (I haven't gotten numbers on that yet, but it's a pretty safe bet :-).

What about access times? A bit surprising, but access times especially for 
smaller sized maps upto 10_000 entries are very comparable between Guava and 
TTTree. Starting at about 10_000 entries my tests showed that Guava is gaining 
an increasingly significant edge over TTTree in raw access speed (better cache 
locality due to its compact array-based representation?). 

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

