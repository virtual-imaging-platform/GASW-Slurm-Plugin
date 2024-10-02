package fr.insalyon.creatis.gasw.executor.slurm.internals.commands;

public class RemoteCommandAlternative<A, B> {
    
    final private String    data;
    final private boolean   evaluator;
    final private Class<A>  classA;
    final private Class<B>  classB;

    public RemoteCommandAlternative(boolean evaluator, Class<A> classA, Class<B> classB, String data) {
        this.data = data;
        this.evaluator = evaluator;
        this.classA = classA;
        this.classB = classB;
    }

    /**
     * will return classA constructor if not evaluator
     * @return
     */
    public RemoteCommand getCommand() {
        try {
            if (evaluator) {
                return (RemoteCommand) classA.getDeclaredConstructors()[0].newInstance(data);
            } else {
                return (RemoteCommand) classB.getDeclaredConstructors()[0].newInstance(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        }
        return null;
    }
}
