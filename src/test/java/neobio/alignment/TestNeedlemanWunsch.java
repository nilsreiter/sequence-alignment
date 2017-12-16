package neobio.alignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestNeedlemanWunsch {
	@Test
	public void test() throws IncompatibleScoringSchemeException {
		List<Integer> s1 = Arrays.asList(1,2,3,4,5,6,7);
		List<Integer> s2 = Arrays.asList(1,2,4,5,7);
		
		NeedlemanWunsch<Integer,String> nw = new NeedlemanWunsch<Integer,String>();
		nw.setMATCH_TAG("==");
		nw.setMISMATCH_TAG("!=");
		nw.setAPPROXIMATE_MATCH_TAG("~~");
		nw.setGAP_TAG("__");
		
		nw.setScoringScheme(new BasicScoringScheme<Integer>(2,1,-1));
		nw.setSeq1(s1);
		nw.setSeq2(s2);
		
		PairwiseAlignment<Integer,String> pa = nw.getPairwiseAlignment();
		assertNotNull(pa);
		assertNotNull(pa.getGappedSequence1());
		assertNotNull(pa.getGappedSequence2());
		assertEquals(pa.getGappedSequence1().size(), pa.getGappedSequence2().size());
		for (int i =0; i < pa.getScoreTagLine().size(); i++) {
			System.out.println(pa.getGappedSequence1().get(i)+ " " + pa.getScoreTagLine().get(i)+" " + pa.getGappedSequence2().get(i));
		}
	}
}
