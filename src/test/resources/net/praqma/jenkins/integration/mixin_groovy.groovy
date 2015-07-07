import net.praqma.jenkins.memorymap.result.*
import java.util.regex.Matcher
import java.util.regex.Pattern


def myList = new ArrayList<MemoryMapConfigMemoryItem>()
def sectionName = mapfile.length()
def myMatcher = Pattern.compile(/_heapsize =\s(.*)$/, Pattern.MULTILINE).matcher(mapfile)
def myHeap = ""
while(myMatcher.find()) {
    myHeap = new MemoryMapConfigMemoryItem("Heap_custom", "", myMatcher.group(1))
}

def myItem = new MemoryMapConfigMemoryItem("Custom ${sectionName}", "", "0xFFFF")
myList.add(myItem)
myList.add(myHeap)
return myList