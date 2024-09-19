package fr.insalyon.creatis.gasw.executor.slurm.internals;

import fr.insalyon.creatis.gasw.executor.slurm.config.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class RemoteStream {
    
    final String    content;

    public String[] getLines() {
        return content.split("\n");
    }

    /**
     * @return the splited row(index)
     */
    public String[] getRow(int index){
        String[] splited = getLines();

        if (index >= splited.length)
            throw new IndexOutOfBoundsException();
        return splited[index].split(Constants.splitRegex);
    }

    /**
     * We assume that all rows have the same format !
     * @return the splited column(index)
     */
    public String[] getColumn(int index) {
        String[] first = getRow(0);
        String[] result = new String[getLines().length];

        if (index > first.length)
            throw new IndexOutOfBoundsException();
        for (int i = 0; i < getLines().length; i++) {
            result[i] = getRow(i)[index];
        }
        return result;
    }
}
