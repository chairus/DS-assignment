/**
 * This class is used to run perform some saveing of values to a file on disk.
 * @author cyrusvillacampa
 */

public class ShutDownHook {
    Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
            // TODO: Write some code to save acceptedProposal(current accepted proposal number),
            //       acceptedValue(current accepted/chosen value) and minProposal.
        }
    })
}
