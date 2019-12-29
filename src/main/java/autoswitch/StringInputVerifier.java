package autoswitch;

import org.aeonbits.owner.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringInputVerifier implements Tokenizer {

    @Override
    public String[] tokens(String values) {
        //Initialize variables and set default
        String defaultsString = "shears > swords > fortPicks > silkPicks > picks > silkAxes > fortAxes > axes > silkShovels > shovels > banes > smites > sharps > tridents > impalingTridents";
        String[] defaults = defaultsString.split(" > ");
        String[] options = values.replace(" ", "").split(">");
        ArrayList<String> optionsArray = new ArrayList<>(Arrays.asList(options));
        List<String> defaultsArray = Arrays.asList(defaults);

        //Clean input
        for (String defaultOption : defaultsArray) {
            if (!optionsArray.contains(defaultOption)){
                System.out.println("AutoSwitchConfig: Added missing option: " + defaultOption);
                optionsArray.add(defaultOption);

            }
        }
        for (String option : optionsArray) {
            if (!defaultsArray.contains(option)){
                System.out.println("AutoSwitchConfig: Removed errant option: " + option);
                optionsArray.remove(option);

            }
        }
        
        String[] temp = new String[optionsArray.size()];
        temp = optionsArray.toArray(temp);
        return temp;
    }
}
