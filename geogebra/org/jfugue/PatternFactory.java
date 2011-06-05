package org.jfugue;

/**
 * This class is used to generate pre-defined Patterns during runtime.  A perfect use of
 * this class would be to extend it and make your new class generate rhythms of various
 * music styles, like Rock, Swing, or 8-Beat.  You could also use it to generate a
 * series of tones or notes that uniquely identifies your product.
 *
 *@author David Koelle
 *@version 2.0
 */
public abstract class PatternFactory
{
    public abstract Pattern getPattern(int selection);
    public abstract int getNumberOfPatterns();

    /**
     * Takes all of the patterns generated by the PatternFactory, and adds them together
     * into one Pattern that can be used to hear all of the music the PatternFactory is
     * capable of producing.
     */
    public Pattern getDemo()
    {
        Pattern returnPattern = new Pattern();

        for (int i=0; i < getNumberOfPatterns(); i++) {
            returnPattern.add(getPattern(i));
        }

        return returnPattern;
    }
}
