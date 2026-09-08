public class Task {
    private String titlu;
    private int durata;
    private boolean completed;

    public Task(String titlu, int durata) {
        this.titlu = titlu;
        this.durata = durata;
        completed  = false;
    }

    public String getTitlu() {
        return titlu;
    }

    public int getDurata() {
        return durata;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }
}
