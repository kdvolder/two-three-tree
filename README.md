Two-Three Tree
==============

Implementation of a persistent, immutable key-value store.

The store is immutable. This means that adding/removing a 
key-value pair doesn't alter the store but instead creates a 
copy.

Because the store is based on a tree and is entirely built
out of immutable nodes. The copy can be made efficiently,
reusing most of the original tree's nodes to build the copy.
Roughly speaking the operation will end up making copies 
of nodes on a path from the tree's root down to one of its 
leaves. So the amount of copying is O(log(n)).