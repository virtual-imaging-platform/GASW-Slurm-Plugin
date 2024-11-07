package fr.insalyon.creatis.gasw.executor.batch.internals.commands;

import java.lang.reflect.InvocationTargetException;

import lombok.extern.log4j.Log4j;

@Log4j
public class RemoteCommandAlternative<A, B> {
    
    final private String    data;
    final private boolean   evaluator;
    final private Class<A>  classA;
    final private Class<B>  classB;

    /**
     * @implNote If classA or classB constructor is different than constr(string e), it will not work
     */
    public RemoteCommandAlternative(final boolean evaluator, final Class<A> classA, final Class<B> classB, final String data) {
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
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("Failed to build the command (using alternative)", e);
        }
        return null;
    }
}
