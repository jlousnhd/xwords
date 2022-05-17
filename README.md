# xwords
Xwords is a word-finder for crossword-style games that I developed in 2014 to
best my family at the popular board game Scrabble.  It is written in Java and
makes use of bit sets and set theory to efficiently search for playable moves.
The user interface was written using Swing, and move searches occur in real
time on a separate thread, resulting in a fast and highly responsive user
interface.

## Move Searches
When a word dictionary is loaded (typically when the program first starts),
xwords creates lookup tables as bit sets based on the positions of letters in
each word.  For example, suppose there are 10,000 words that are 5 letters
long.  Xwords will create 130 arrays (5 x 26) for these words, each 10,000 bits
in length.  If the word at index 4,356 has an 'R' in the fifth position, the
array associated with 'R' and index 4 will have its 4,356th bit set.  The
reason for this lookup table generation will become clear later.

Searches work by dividing the current board state into 'slots' which represent
locations where a word could theoretically be played.  Each slot is 2-15 tiles
in length.  At the start of the game, there are a couple dozen valid slots
surrounding the center tile, none of which contain any letters.  After the
first word is played, playable slots surround the letters on the board,
containing or adjacent to the letters played.

![Example of a slot, highlighted](/xwords-slot.png)

A slot highlighted in red is shown above.  Each slot has a set length and
typically contains one or more fixed letters.  (The highlighted slot is of
length 5 and contains an 'R' in the fifth position.)  It would normally take a
non-trivial amount of time to iterate through several thousand words and create
a list of the ones that contain an 'R' at a certain index.  However, thanks to
the lookup tables that were created when the dictionary was loaded, this list
has already been made for us.  What's more, if we have a more complex slot, for
example one which has three letters already set, we can simply retrieve the
lookup tables for those three letters and perform a bitwise AND operation on
them to rapidly generate a set that contains only words which fit the slot.

After a slot's playable words have been determined, xwords looks at the empty
tiles in the slot and creates a bit set of words that the player's hand could
fill in for that slot.  (This is done iteratively.)  The resulting set is again
AND'ed with the playable words for that slot to determine exactly which words
both fit the slot and can be formed from the player's hand.  After this, checks
are done to determine if there are any adjacent slots (for example, when
forming multiple words in one play), and xwords checks to see if the attached
words are valid.  If everything checks out, playable moves are returned and
further processed to determine scores.

Thanks to these pre-generated lookup tables, xwords is able to perform word
searches extremely quickly.  A bitwise AND operation on a few thousand bits can
be executed faster than most set intersection operations for the number of
entries typically encountered.  The intended target of this program was a 2009
netbook running one of Intel's low-powered Atom CPUs, and search results for
a complex board layout with a standard Scrabble dictionary could be calculated
nearly as quickly as the user could enter letters into the interface.

The memory requirements for these lookup tables are also quite modest, only
necessitating around 4.5 MiB for a dictionary of over 178,000 words.
