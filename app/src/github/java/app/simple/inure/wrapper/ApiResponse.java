package app.simple.inure.wrapper;

public class ApiResponse <T> {
    private DataWrapper <T> data;
    
    public DataWrapper <T> getData() {
        return data;
    }
    
    public void setData(DataWrapper <T> data) {
        this.data = data;
    }
    
    public static class DataWrapper <T> {
        private T attributes;
        
        public T getAttributes() {
            return attributes;
        }
        
        public void setAttributes(T attributes) {
            this.attributes = attributes;
        }
    }
}

