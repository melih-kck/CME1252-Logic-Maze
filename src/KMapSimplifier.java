// Simplifies a 4-variable boolean function using the Quine-McCluskey algorithm.
public final class KMapSimplifier
{
    // Safe upper bounds for a 4-variable (16-row) truth table.
    // at most 16 (one per row).
    // the Quine-McCluskey expansion never exceeds 64 for 4 variables.
    private static final int MAX_MINTERMS   = 16;
    private static final int MAX_IMPLICANTS = 64;

    private KMapSimplifier()
    {
    }

    // Takes a boolean[16] truth table, returns simplified SOP expression string.
    public static String simplify(boolean[] table)
    {
        if (table == null || table.length != 16)
        {
            return "";
        }

        int[] minterms     = new int[MAX_MINTERMS];
        int   mintermCount = 0;
        int   universe     = 0;

        for (int rowIndex = 0; rowIndex < table.length; rowIndex++)
        {
            if (table[rowIndex])
            {
                minterms[mintermCount] = rowIndex;
                mintermCount           = mintermCount + 1;
                universe               = universe | (1 << rowIndex);
            }
        }

        if (mintermCount == 0)
        {
            return "0";
        }
        if (mintermCount == 16)
        {
            return "1";
        }

        //  prime implicants
        Implicant[] primes     = new Implicant[MAX_IMPLICANTS];
        int         primeCount = findPrimeImplicants(minterms, mintermCount, primes);

        // cover selection
        Implicant[] selected      = new Implicant[MAX_IMPLICANTS];
        int         selectedCount = selectCover(primes, primeCount, minterms, mintermCount, universe, selected);

        String[] terms = new String[selectedCount];
        for (int selectedIndex = 0; selectedIndex < selectedCount; selectedIndex++)
        {
            terms[selectedIndex] = selected[selectedIndex].toTerm();
        }
        sortTerms(terms);

        String result = "";
        for (int termIndex = 0; termIndex < terms.length; termIndex++)
        {
            if (termIndex > 0)
            {
                result = result + " v ";
            }
            result = result + terms[termIndex];
        }
        return result;
    }

    // Writes results into the primes[] array and returns how many were written.
    private static int findPrimeImplicants(int[] minterms, int mintermCount, Implicant[] primes)
    {
        int primeCount = 0;

        // Seed the first round with one implicant per minterm.
        Implicant[] current      = new Implicant[MAX_IMPLICANTS];
        int         currentCount = 0;

        for (int mintermIndex = 0; mintermIndex < mintermCount; mintermIndex++)
        {
            int term              = minterms[mintermIndex];
            current[currentCount] = new Implicant(term, 0, 1 << term);
            currentCount          = currentCount + 1;
        }

        while (currentCount > 0)
        {
            boolean[]   used      = new boolean[currentCount];
            Implicant[] next      = new Implicant[MAX_IMPLICANTS];
            int         nextCount = 0;

            for (int firstIndex = 0; firstIndex < currentCount; firstIndex++)
            {
                for (int secondIndex = firstIndex + 1; secondIndex < currentCount; secondIndex++)
                {
                    Implicant combined = current[firstIndex].combine(current[secondIndex]);
                    if (combined != null)
                    {
                        used[firstIndex]  = true;
                        used[secondIndex] = true;
                        nextCount         = addUnique(next, nextCount, combined);
                    }
                }
            }

            // Any implicant that was never merged is a prime implicant.
            for (int currentIndex = 0; currentIndex < currentCount; currentIndex++)
            {
                if (!used[currentIndex])
                {
                    primeCount = addUnique(primes, primeCount, current[currentIndex]);
                }
            }

            current      = next;
            currentCount = nextCount;
        }

        return primeCount;
    }

    // Writes chosen implicants into result[] and returns how many were written.
    private static int selectCover(Implicant[] primes, int primeCount,
                                   int[] minterms, int mintermCount,
                                   int universe, Implicant[] result)
    {
        boolean[] selected = new boolean[primeCount];
        int       covered  = 0;

        // Find essential prime implicants (those that uniquely cover a minterm).
        for (int mintermIndex = 0; mintermIndex < mintermCount; mintermIndex++)
        {
            int minterm   = minterms[mintermIndex];
            int onlyIndex = -1;
            int count     = 0;

            for (int primeIndex = 0; primeIndex < primeCount; primeIndex++)
            {
                if (primes[primeIndex].covers(minterm))
                {
                    onlyIndex = primeIndex;
                    count     = count + 1;
                }
            }

            if (count == 1 && onlyIndex != -1)
            {
                selected[onlyIndex] = true;
            }
        }

        for (int primeIndex = 0; primeIndex < primeCount; primeIndex++)
        {
            if (selected[primeIndex])
            {
                covered = covered | primes[primeIndex].covered;
            }
        }

        // Collect non-essential prime implicants.
        int[] optional      = new int[primeCount];
        int   optionalCount = 0;

        for (int primeIndex = 0; primeIndex < primeCount; primeIndex++)
        {
            if (!selected[primeIndex])
            {
                optional[optionalCount] = primeIndex;
                optionalCount           = optionalCount + 1;
            }
        }

        // Brute-force all subsets of optional implicants to cover remaining minterms.
        int remaining        = universe & ~covered;
        int bestMask         = 0;
        int bestLiteralCost  = Integer.MAX_VALUE;
        int bestTermCost     = Integer.MAX_VALUE;

        int combinations = 1 << optionalCount;
        for (int mask = 0; mask < combinations; mask++)
        {
            int subsetCover = 0;
            int literalCost = 0;
            int termCost    = 0;

            for (int bit = 0; bit < optionalCount; bit++)
            {
                if ((mask & (1 << bit)) != 0)
                {
                    Implicant candidate = primes[optional[bit]];
                    subsetCover         = subsetCover | candidate.covered;
                    literalCost         = literalCost + candidate.literalCount();
                    termCost            = termCost + 1;
                }
            }

            if ((subsetCover & remaining) == remaining)
            {
                if (literalCost < bestLiteralCost
                        || (literalCost == bestLiteralCost && termCost < bestTermCost))
                {
                    bestMask        = mask;
                    bestLiteralCost = literalCost;
                    bestTermCost    = termCost;
                }
            }
        }

        // Build the final result: essentials first, then chosen optionals.
        int resultCount = 0;

        for (int primeIndex = 0; primeIndex < primeCount; primeIndex++)
        {
            if (selected[primeIndex])
            {
                resultCount = addUnique(result, resultCount, primes[primeIndex]);
            }
        }

        for (int bit = 0; bit < optionalCount; bit++)
        {
            if ((bestMask & (1 << bit)) != 0)
            {
                resultCount = addUnique(result, resultCount, primes[optional[bit]]);
            }
        }

        return resultCount;
    }

    // Adds item to list only if no element with the same bit pattern already exists.
    // Returns the new count.
    private static int addUnique(Implicant[] list, int count, Implicant item)
    {
        for (int itemIndex = 0; itemIndex < count; itemIndex++)
        {
            if (list[itemIndex].samePattern(item))
            {
                return count;
            }
        }
        list[count] = item;
        return count + 1;
    }

    private static void sortTerms(String[] terms)
    {
        for (int firstIndex = 0; firstIndex < terms.length - 1; firstIndex++)
        {
            for (int secondIndex = firstIndex + 1; secondIndex < terms.length; secondIndex++)
            {
                if (terms[secondIndex].length() < terms[firstIndex].length()
                        || (terms[secondIndex].length() == terms[firstIndex].length()
                        && terms[secondIndex].compareTo(terms[firstIndex]) < 0))
                {
                    String swappedTerm   = terms[firstIndex];
                    terms[firstIndex]    = terms[secondIndex];
                    terms[secondIndex]   = swappedTerm;
                }
            }
        }
    }

    // Represents a product term. bits/mask encode the 4-variable pattern.
    private static final class Implicant
    {
        // mask bit 1 means that variable is a don't care in this implicant.
        private final int bits;
        private final int mask;
        private final int covered;

        Implicant(int bits, int mask, int covered)
        {
            this.bits    = bits;
            this.mask    = mask;
            this.covered = covered;
        }

        Implicant combine(Implicant other)
        {
            if (mask != other.mask)
            {
                return null;
            }
            int diff = (bits ^ other.bits) & ~mask;
            if (Integer.bitCount(diff) != 1)
            {
                return null;
            }
            return new Implicant(bits & ~diff, mask | diff, covered | other.covered);
        }

        boolean samePattern(Implicant other)
        {
            return bits == other.bits && mask == other.mask;
        }

        boolean covers(int minterm)
        {
            return (minterm & ~mask) == (bits & ~mask);
        }

        int literalCount()
        {
            return 4 - Integer.bitCount(mask);
        }

        String toTerm()
        {
            if (mask == 15)
            {
                return "1";
            }

            char[] variableNames = {'A', 'B', 'C', 'D'};
            int[]  bitValues     = {8, 4, 2, 1};
            String text          = "";

            for (int variableIndex = 0; variableIndex < variableNames.length; variableIndex++)
            {
                if ((mask & bitValues[variableIndex]) == 0)
                {
                    text = text + variableNames[variableIndex];
                    if ((bits & bitValues[variableIndex]) == 0)
                    {
                        text = text + "'";
                    }
                }
            }
            return text;
        }
    }
}