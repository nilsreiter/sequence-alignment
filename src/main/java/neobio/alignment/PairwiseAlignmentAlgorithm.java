/*
 * PairwiseAlignmentAlgorithm.java
 *
 * Copyright 2003 Sergio Anibal de Carvalho Junior
 *
 * This file is part of NeoBio.
 *
 * NeoBio is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * NeoBio is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NeoBio;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Proper attribution of the author as the source of the software would be appreciated.
 *
 * Sergio Anibal de Carvalho Junior		mailto:sergioanibaljr@users.sourceforge.net
 * Department of Computer Science		http://www.dcs.kcl.ac.uk
 * King's College London, UK			http://www.kcl.ac.uk
 *
 * Please visit http://neobio.sourceforge.net
 *
 * This project was supervised by Professor Maxime Crochemore.
 *
 */

package neobio.alignment;

/**
 * This abstract class is the superclass of all classes implementing pairwise sequence
 * alignment algorithms. Subclasses are required to provide methods to build a high
 * scoring alignment between two sequences and compute its score with a given scoring
 * scheme.
 *
 * <P>Clients are required to set a scoring scheme and load two sequences before
 * requesting an alignment or the computation of its score. They typically make the
 * following sequence of method calls:</P>
 *
 * <CODE><BLOCKQUOTE><PRE>
 * // prepare
 * PairwiseAlignmentAlgorithm algorithm = new SomePairwiseAlignmentAlgorith ();
 * algorithm.setScoringScheme (some_scoring_scheme);
 * algorithm.loadSequences (sequence1, sequence2);
 *
 * // now compute the alignment
 * PairwiseAlignment alignment = algorithm.getPairwiseAlignment();
 * int score = algorithm.getScore();
 * </PRE></BLOCKQUOTE></CODE>
 *
 * @author Sergio A. de Carvalho Jr.
 * @see PairwiseAlignment
 */
public abstract class PairwiseAlignmentAlgorithm<T, S>
{
	/**
	 * Tag character that signals a match in the score tag line of an alignment. Its use
	 * is conditioned by the <CODE>use_match_tag</CODE> flag.
	 *
	 * @see #use_match_tag
	 * @see #useMatchTag
	 */
	S MATCH_TAG = null;

	/**
	 * Tag character that signals an approximate match in the score tag line of an
	 * alignment.
	 */
	
	S APPROXIMATE_MATCH_TAG = null;

	/**
	 * Character that signals a mismatch in the score tag line of an alignment.
	 */
	S MISMATCH_TAG = null;

	/**
	 * Character that signals a gap in the score tag line of an alignment.
	 */
	S GAP_TAG = null;

	/**
	 * Character that signals a gap in sequence.
	 */
	protected  final T GAP_CHARACTER = null;

	/**
	 * Indicates if the <CODE>MATCH_TAG</CODE> tag should be used or not. If it is
	 * <CODE>true</CODE>, the alignment algorithm should write the <CODE>MATCH_TAG</CODE>
	 * tag in the score tag line of the alignment whenever a match occurs between
	 * characters of the two sequences. If it is <CODE>false</CODE> the matching character
	 * should be written instead. This flag is updated whenever a scoring scheme is set to
	 * this <CODE>PairwiseAlignmentAlgorithm</CODE> by the <CODE>setScoringScheme</CODE>
	 * method.
	 *
	 * @see #MATCH_TAG
	 * @see #useMatchTag
	 * @see #setScoringScheme
	 */
	protected boolean use_match_tag;

	/**
	 * The scoring scheme used to compute a pairwise sequence alignment. It must be set
	 * before performing the alignment, and if a new scoring scheme is set, any alignment
	 * or score already computed is lost.
	 */
	protected ScoringScheme<T> scoring;

	/**
	 * Stores the product of the last pairwise alignment performed. It contains a string
	 * representation of a highest scoring alignment between the two sequences and its
	 * score. It is set after a successful execution of the
	 * <CODE>computePairwiseAlignment</CODE> method that subclasses must implement. It is
	 * set to null if new sequences are loaded or a new scoring scheme is set.
	 */
	protected PairwiseAlignment<T, S> alignment;

	/**
	 * This field stores just the score of the last pairwise alignment performed (if the
	 * <CODE>score_computed flag</CODE> is set to true). It is useful when just the score
	 * is needed (and not the alignment itselft). Its value is set after a successful
	 * execution of both <CODE>computePairwiseAlignment</CODE> or
	 * <CODE>computeScore</CODE> methods that subclasses must implement. If new sequences
	 * are loaded or a new scoring scheme is set, the <CODE>score_computed</CODE> flag is
	 * set to false, and this field's value becomes undefined.
	 */
	protected int score;

	/**
	 * Flags whether the score of the alignment between the last two loaded sequences has
	 * already been computed. It is set to true after a successful execution of both
	 * <CODE>computePairwiseAlignment</CODE> or <CODE>computeScore</CODE> methods that
	 * subclasses must implement. It is set to falsef if new sequences are loaded or a new
	 * scoring scheme is set.
	 */
	protected boolean score_computed = false;

	/**
	 * Flags whether sequences have been loaded. It is set to true after subclasses
	 * successfully load two sequences.
	 */
	protected boolean sequences_loaded = false;

	/**
	 * Sets the scoring scheme to be used for the next alignments. Any alignment or score
	 * already computed is lost. If the scoring scheme supports partial matches, this
	 * <CODE>PairwiseAlignmentAlgorithm</CODE> is set not to use the
	 * <CODE>MATCH_TAG</CODE> tag because in this case the score tag line be confusing.
	 * If the scoring scheme does not support partial matches, then the use of the
	 * <CODE>MATCH_TAG</CODE> tag is enabled.
	 *
	 * @param scoring Scoring scheme to be used
	 * @see #MATCH_TAG
	 * @see ScoringScheme#isPartialMatchSupported
	 */
	public void setScoringScheme (ScoringScheme<T> scoring)
	{
		if (scoring == null)
			throw new IllegalArgumentException ("Null scoring scheme object.");

		this.scoring = scoring;

		// if the scoring scheme supports partial matches,
		// disable the use of the MATCH_TAG character
		if (scoring.isPartialMatchSupported())
			this.use_match_tag = false;
		else
			this.use_match_tag = true;

		// when a new scoring scheme is set,
		// the alignment needs to be recomputed
		this.alignment = null;
		this.score_computed = false;
	}

	/**
	 * Tells wether the <CODE>MATCH_TAG</CODE> tag should be used or not. If it returns
	 * <CODE>true</CODE>, the alignment algorithm should write the <CODE>MATCH_TAG</CODE>
	 * tag in the score tag line of the alignment produced whenever a match occurs between
	 * characters of the two sequences. If it returns <CODE>false</CODE> the matching
	 * character should be written instead. The value returned is conditioned by the
	 * <CODE>use_match_tag</CODE> flag, which is updated whenever a scoring scheme is set
	 * to this <CODE>PairwiseAlignmentAlgorithm</CODE> by the
	 * <CODE>setScoringScheme</CODE> method.
	 *
	 * @return <CODE>true</CODE if the <CODE>MATCH_TAG</CODE> tag should be used,
	 * <CODE>false</CODE> otherwise
	 * @see #MATCH_TAG
	 * @see #use_match_tag
	 * @see #setScoringScheme
	 */
	protected boolean useMatchTag ()
	{
		return use_match_tag;
	}

	/**
	 * Return the last pairwise alignment computed (if any) or request subclasses to
	 * compute one and return the result by calling the
	 * <CODE>computePairwiseAlignment</CODE> method. The sequences must already be loaded
	 * and a scoring scheme must already be set.
	 *
	 * @return a pairwise alignment between the loaded sequences
	 * @throws IncompatibleScoringSchemeException If the scoring scheme
	 * is not compatible with the loaded sequences
	 * @see #computePairwiseAlignment
	 */
	public PairwiseAlignment<T, S> getPairwiseAlignment ()
		throws IncompatibleScoringSchemeException
	{

		// TODO: Test for gap tags
		
		if (scoring == null)
			throw new IllegalStateException ("Scoring scheme has not been set.");

		if (this.alignment == null)
		{
			// make sure the scoring scheme won't be changed
			// in the middle of the alignment computation
			synchronized (scoring)
			{
				// compute the alignment if it hasn't been computed yet
				this.alignment = computePairwiseAlignment();
			}

			// store the score as well
			this.score = this.alignment.getScore();
			this.score_computed = true;
		}

		return this.alignment;
	}

	/**
	 * Returns the score of the last alignment computed (if any) or request subclasses to
	 * compute one and return the result by calling the <CODE>computeScore</CODE> method.
	 * The sequences must already be loaded and a scoring scheme must already be set.
	 *
	 * @return the score of the alignment between the loaded sequences
	 * @throws IncompatibleScoringSchemeException If the scoring scheme
	 * is not compatible with the loaded sequences
	 * @see #computeScore
	 */
	public int getScore () throws IncompatibleScoringSchemeException
	{
		if (!sequences_loaded)
			throw new IllegalStateException ("Sequences have not been loaded.");

		if (scoring == null)
			throw new IllegalStateException ("Scoring scheme has not been set.");

		if (!score_computed)
		{
			// make sure the scoring scheme won't be changed
			// in the middle of the alignment computation
			synchronized (scoring)
			{
				// compute the alignment's score if it hasn't been computed yet
				this.score = computeScore();
			}

			this.score_computed = true;
		}

		return this.score;
	}


	/**
	 * Subclasses must implement this method to compute an alignment between the loaded
	 * sequences using the scoring scheme previously set. This methid is called by the
	 * <CODE>getPairwiseAlignment</CODE> method when needed.
	 *
	 * @return a pairwise alignment between the loaded sequences
	 * @throws IncompatibleScoringSchemeException If the scoring scheme
	 * is not compatible with the loaded sequences
	 * @see #getPairwiseAlignment
	 */
	protected abstract PairwiseAlignment<T, S> computePairwiseAlignment ()
		throws IncompatibleScoringSchemeException;

	/**
	 * Subclasses must implement this method to compute the score of the alignment between
	 * the loaded sequences using the scoring scheme previously set. This methid is called
	 * by the <CODE>getScore</CODE> method when needed.
	 *
	 * @return the score of the alignment between the loaded sequences
	 * @throws IncompatibleScoringSchemeException If the scoring scheme
	 * is not compatible with the loaded sequences
	 * @see #getScore
	 */
	protected abstract int computeScore () throws IncompatibleScoringSchemeException;

	/**
	 * Helper method to invoke the <CODE>scoreSubstitution</CODE> method of the scoring
	 * scheme set to this algorithm.
	 *
	 * @param a first character
	 * @param b second character
	 * @return score of substitution of <CODE>a</CODE> for <CODE>b</CODE>
	 * @throws IncompatibleScoringSchemeException if the scoring scheme is not compatible
	 * with the sequences being aligned
	 * @see ScoringScheme#scoreSubstitution
	 */
	protected final int scoreSubstitution (T a, T b)
		throws IncompatibleScoringSchemeException
	{
		return scoring.scoreSubstitution (a, b);
	}

	/**
	 * Helper method to invoke the <CODE>scoreInsertion</CODE> method of the scoring
	 * scheme set to this algorithm.
	 *
	 * @param a the character to be inserted
	 * @return score of insertion of <CODE>a</CODE>
	 * @throws IncompatibleScoringSchemeException if the scoring scheme is not compatible
	 * with the sequences being aligned
	 * @see ScoringScheme#scoreInsertion
	 */
	protected final int scoreInsertion (T a) throws IncompatibleScoringSchemeException
	{
		return scoring.scoreInsertion (a);
	}

	/**
	 * Helper method to invoke the <CODE>scoreDeletion</CODE> method of the scoring scheme
	 * set to this algorithm.
	 *
	 * @param a the character to be deleted
	 * @return score of deletion of <CODE>a</CODE>
	 * @throws IncompatibleScoringSchemeException if the scoring scheme is not compatible
	 * with the sequences being aligned
	 * @see ScoringScheme#scoreDeletion
	 */
	protected final int scoreDeletion (T a) throws IncompatibleScoringSchemeException
	{
		return scoring.scoreDeletion (a);
	}

	/**
	 * Helper method to compute the the greater of two values.
	 *
	 * @param v1 first value
	 * @param v2 second value
	 * @return the larger of <CODE>v1</CODE> and <CODE>v2</CODE>
	 */
	protected final int max (int v1, int v2)
	{
		return (v1 >= v2) ? v1 : v2;
	}

	/**
	 * Helper method to compute the the greater of three values.
	 *
	 * @param v1 first value
	 * @param v2 second value
	 * @param v3 third value
	 * @return the larger of <CODE>v1</CODE>, <CODE>v2</CODE> and <CODE>v3</CODE>
	 */
	protected final int max (int v1, int v2, int v3)
	{
		return (v1 >= v2) ? ((v1 >= v3)? v1 : v3) : ((v2 >= v3)? v2 : v3);
	}

	/**
	 * Helper method to compute the the greater of four values.
	 *
	 * @param v1 first value
	 * @param v2 second value
	 * @param v3 third value
	 * @param v4 fourth value
	 * @return the larger of <CODE>v1</CODE>, <CODE>v2</CODE> <CODE>v3</CODE> and
	 * <CODE>v4</CODE>
	 */
	protected final int max (int v1, int v2, int v3, int v4)
	{
		int m1 = ((v1 >= v2) ? v1 : v2);
		int m2 = ((v3 >= v4) ? v3 : v4);

		return (m1 >= m2) ? m1 : m2;
	}

	public S getMATCH_TAG() {
		return MATCH_TAG;
	}

	public void setMATCH_TAG(S mATCH_TAG) {
		MATCH_TAG = mATCH_TAG;
	}

	public S getAPPROXIMATE_MATCH_TAG() {
		return APPROXIMATE_MATCH_TAG;
	}

	public void setAPPROXIMATE_MATCH_TAG(S aPPROXIMATE_MATCH_TAG) {
		APPROXIMATE_MATCH_TAG = aPPROXIMATE_MATCH_TAG;
	}

	public S getMISMATCH_TAG() {
		return MISMATCH_TAG;
	}

	public void setMISMATCH_TAG(S mISMATCH_TAG) {
		MISMATCH_TAG = mISMATCH_TAG;
	}

	public S getGAP_TAG() {
		return GAP_TAG;
	}

	public void setGAP_TAG(S gAP_TAG) {
		GAP_TAG = gAP_TAG;
	}

	public T getGAP_CHARACTER() {
		return GAP_CHARACTER;
	}
}
