import java.util.ArrayList;

// Simplifies a 4-variable boolean function using the Quine-McCluskey algorithm.
public final class KMapSimplifier
{
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

        ArrayList<Integer> minterms = new ArrayList<Integer>();
        int universe = 0;
        for (int rowIndex = 0; rowIndex < table.length; rowIndex++)
        {
            if (table[rowIndex])
            {
                minterms.add(Integer.valueOf(rowIndex));
                universe = universe | (1 << rowIndex);
            }
        }

        if (minterms.isEmpty())
        {
            return "0";
        }
        if (minterms.size() == 16)
        {
            return "1";
        }

        ArrayList<Implicant> primes = findPrimeImplicants(minterms);
        ArrayList<Implicant> selected = selectCover(primes, minterms, universe);

        String[] terms = new String[selected.size()];
        for (int selectedIndex = 0; selectedIndex < selected.size(); selectedIndex++)
        {
            terms[selectedIndex] = selected.get(selectedIndex).toTerm();
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

    // Stage 1: Generates all prime implicants by combining minterms that differ by one bit.
    private static ArrayList<Implicant> findPrimeImplicants(ArrayList<Integer> minterms)
    {
        ArrayList<Implicant> current = new ArrayList<Implicant>();
        for (int mintermIndex = 0; mintermIndex < minterms.size(); mintermIndex++)
        {
            int term = minterms.get(mintermIndex).intValue();
            current.add(new Implicant(term, 0, 1 << term));
        }

        ArrayList<Implicant> primes = new ArrayList<Implicant>();
        while (!current.isEmpty())
        {
            boolean[] used = new boolean[current.size()];
            ArrayList<Implicant> next = new ArrayList<Implicant>();

            for (int firstIndex = 0; firstIndex < current.size(); firstIndex++)
            {
                for (int secondIndex = firstIndex + 1; secondIndex < current.size(); secondIndex++)
                {
                    Implicant combined = current.get(firstIndex).combine(current.get(secondIndex));
                    if (combined != null)
                    {
                        used[firstIndex] = true;
                        used[secondIndex] = true;
                        addUnique(next, combined);
                    }
                }
            }

            for (int currentIndex = 0; currentIndex < current.size(); currentIndex++)
            {
                if (!used[currentIndex])
                {
                    addUnique(primes, current.get(currentIndex));
                }
            }
            current = next;
        }
        return primes;
    }

    // Stage 2: Selects essential prime implicants, then brute-force covers the rest.
    private static ArrayList<Implicant> selectCover(ArrayList<Implicant> primes, ArrayList<Integer> minterms, int universe)
    {
        boolean[] selected = new boolean[primes.size()];
        int covered = 0;

        for (int mintermIndex = 0; mintermIndex < minterms.size(); mintermIndex++)
        {
            int minterm = minterms.get(mintermIndex).intValue();
            int onlyIndex = -1;
            int count = 0;
            for (int primeIndex = 0; primeIndex < primes.size(); primeIndex++)
            {
                if (primes.get(primeIndex).covers(minterm))
                {
                    onlyIndex = primeIndex;
                    count++;
                }
            }
            if (count == 1 && onlyIndex != -1)
            {
                selected[onlyIndex] = true;
            }
        }

        for (int primeIndex = 0; primeIndex < selected.length; primeIndex++)
        {
            if (selected[primeIndex])
            {
                covered = covered | primes.get(primeIndex).covered;
            }
        }

        ArrayList<Integer> optional = new ArrayList<Integer>();
        for (int primeIndex = 0; primeIndex < primes.size(); primeIndex++)
        {
            if (!selected[primeIndex])
            {
                optional.add(Integer.valueOf(primeIndex));
            }
        }

        int remaining = universe & ~covered;
        int bestMask = 0;
        int bestLiteralCost = Integer.MAX_VALUE;
        int bestTermCost = Integer.MAX_VALUE;

        int combinations = 1 << optional.size();
        for (int mask = 0; mask < combinations; mask++)
        {
            int subsetCover = 0;
            int literalCost = 0;
            int termCost = 0;

            for (int bit = 0; bit < optional.size(); bit++)
            {
                if ((mask & (1 << bit)) != 0)
                {
                    Implicant candidate = primes.get(optional.get(bit).intValue());
                    subsetCover = subsetCover | candidate.covered;
                    literalCost = literalCost + candidate.literalCount();
                    termCost++;
                }
            }

            if ((subsetCover & remaining) == remaining)
            {
                if (literalCost < bestLiteralCost
                || (literalCost == bestLiteralCost && termCost < bestTermCost))
                {
                    bestMask = mask;
                    bestLiteralCost = literalCost;
                    bestTermCost = termCost;
                }
            }
        }

        ArrayList<Implicant> result = new ArrayList<Implicant>();
        for (int primeIndex = 0; primeIndex < selected.length; primeIndex++)
        {
            if (selected[primeIndex])
            {
                addUnique(result, primes.get(primeIndex));
            }
        }
        for (int bit = 0; bit < optional.size(); bit++)
        {
            if ((bestMask & (1 << bit)) != 0)
            {
                addUnique(result, primes.get(optional.get(bit).intValue()));
            }
        }
        return result;
    }

    private static void addUnique(ArrayList<Implicant> list, Implicant item)
    {
        for (int itemIndex = 0; itemIndex < list.size(); itemIndex++)
        {
            if (list.get(itemIndex).samePattern(item))
            {
                return;
            }
        }
        list.add(item);
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
                    String swappedTerm = terms[firstIndex];
                    terms[firstIndex] = terms[secondIndex];
                    terms[secondIndex] = swappedTerm;
                }
            }
        }
    }

    // Represents a product term. bits/mask encode the 4-variable pattern (ABCD = bits 3210).
    private static final class Implicant
    {
        // mask bit 1 means that variable is a don't-care in this implicant.
        private final int bits;
        private final int mask;
        private final int covered;

        Implicant(int bits, int mask, int covered)
        {
            this.bits = bits;
            this.mask = mask;
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
            int[] bitValues = {8, 4, 2, 1};
            String text = "";

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
