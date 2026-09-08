public class FocusSession {
    private Task task;
    private int ore;
    private int minute;
    private boolean activ;

    public FocusSession(Task task, int ore, int minute) {
        this.task = task;
        this.ore = ore;
        this.minute = minute;
        this.activ = false;
    }

    public void start() {
        this.activ = true;
        System.out.println("Sesiune pornita pentru: " + task.getTitlu());
    }

    public void pause() {
        this.activ = false;
        System.out.println("Sesiune opritǎ pentru: " + task.getTitlu());
    }

    public void stop() {
        this.activ = false;
        task.complete();
        System.out.println("Sesiunea pentru: " + task.getTitlu() + " a fost terminata");
    }

    public boolean isActiv() {
        return activ;
    }

    public long getTotalMillis() {
        return (ore * 60L + minute) * 60 * 1000;
    }
}