/* :name=   Stats export CSV :description=\
 *          Dumps raw statistics intto 2 (source and target) files
 * 
 * 
 * Shows statistics for the current project. Keep in mind
 * that the script doesn't discriminate between
 * unique and non-unique segments.
 * (script Hacked from "Show target statistics" from Kos Ivantsov and Briac Pilpre)
 * 
 * @author  Kos Ivantsov, Briac Pilpre, Bruno Baudry
 * @date    2022-05-20
 * @version 0.1
 */

import java.awt.datatransfer.StringSelection
import java.awt.Toolkit
import org.omegat.core.statistics.Statistics
import org.omegat.core.data.ProtectedPart
import org.omegat.core.Core;
import org.omegat.util.StringUtil;
import static javax.swing.JOptionPane.*;

def count_segment (s) {
    if (s == null) return 0

    spaces = /[\u00a0|\p{Blank}|\p{Space}]+/
    w = s.trim().replaceAll(spaces, " ").split(spaces)
    c = w.length

    return c
}
def SEP = ";"; //change it to your CSV separator format
def statsFolderNaming = "stats"; // change it if needed
def nl = System.getProperty('line.separator');
def prop = project.projectProperties
  if (!prop) {
    showMessageDialog null, res.getString("noProjectMsg"), res.getString("noProject"), INFORMATION_MESSAGE
    return
  }
def root = prop.projectRoot;
def timeStamp   = new Date().format("yyyy-MM-dd");
def statsFolder = new File(root + statsFolderNaming + '/');
if(!statsFolder.exists())
{
    statsFolder.mkdir()
}
def targetTextFile = new File(statsFolder, timeStamp + '_target-stats.csv');
def sourceTextFile = new File(statsFolder, timeStamp + '_source-stats.csv');
def countTargets = 0;
perfileTarget = ("Filename $SEP Words $SEP Characters without spaces $SEP Characters with spaces $SEP MSWord words in target $SEP MSWord words in source $nl")
perfileSource = ("Filename $SEP Words $SEP MSWord words in source $nl")

console.clear()

totalWords = 0
totalCharsWithoutSpaces = 0
totalCharsWithSpaces = 0
totalTargetWordsMS = 0
totalSourceWordsMS = 0

files = project.projectFiles
for (i in 0 ..< files.size())
{
	fi = files[i]
	curfilename = fi.filePath
	def words = 0
	def charsWithoutSpaces = 0
	def charsWithSpaces = 0
	def sourceWordsMS = 0
	def targetWordsMS = 0
	for (j in 0 ..< fi.entries.size()) {
		ste = fi.entries[j]
		src = ste.getSrcText()
		targ = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null
		if (targ == null)
		{
			for (ProtectedPart pp : ste.getProtectedParts()) {
				src = src.replace(pp.getTextInSourceSegment(), pp.getReplacementWordsCountCalculation())
			}
			words += Statistics.numberOfWords(src)
			sourceWordsMS += count_segment(src)        
		}
		else{
			countTargets++;
			for (ProtectedPart pp : ste.getProtectedParts()) {
			src = src.replace(pp.getTextInSourceSegment(), pp.getReplacementWordsCountCalculation())
			targ = targ.replace(pp.getTextInSourceSegment(), pp.getReplacementWordsCountCalculation())
		}
		words += Statistics.numberOfWords(targ)
		charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(targ)
		charsWithSpaces += Statistics.numberOfCharactersWithSpaces(targ)
		targetWordsMS += count_segment(targ)
		sourceWordsMS += count_segment(src)
		}
		
	}
	perfileTarget += ("$curfilename $SEP $words $SEP $charsWithoutSpaces $SEP $charsWithSpaces $SEP $targetWordsMS $SEP $sourceWordsMS $nl")
	perfileSource += ("$curfilename $SEP $words $SEP $sourceWordsMS $nl")

}

if(countTargets>0)
	targetTextFile.write(perfileTarget, "UTF-8");

sourceTextFile.write(perfileSource, "UTF-8");
console.println("SOURCE STATS (saved in ${sourceTextFile.absolutePath}):")
console.println(perfileSource.replace(SEP, "\t"))
console.println("TARGET STATS (saved in ${targetTextFile.absolutePath}):")
console.println(perfileTarget.replace(SEP, "\t"))
