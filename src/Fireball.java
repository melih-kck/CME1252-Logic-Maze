    public class Fireball {
        private int x;
        private int y;
        private int dx = 0;
        private int dy = 0;
        private boolean active;
        public Fireball(int startX, int startY, int direction) {
            this.x = startX;
            this.y = startY;
            this.active = true;
            if(direction == Direction.UP)
            {
                this.dy = -1;
            }
            if(direction == Direction.DOWN)
            {
                this.dy = 1;
            }
            if(direction == Direction.LEFT)
            {
                this.dx = -1;
            }
            if(direction == Direction.RIGHT)
            {
                this.dy = 1;
            }
            if (direction == Direction.NONE) {
                this.active = false;
            }
        }
        public int firex ;
        public int firey ;
        public int counter = -1 ;
        public int stfirex = 0 ;
        public int stfirey = 0 ;
        public boolean firealive = false ;
        public double movx = 0 ;
        public double movy = 0 ;
    
        public void update() {
            if (active) {
                this.x += this.dx;
                this.y += this.dy;
            }
        }
        public int getX() { return x; }
        public int getY() { return y; }
        public boolean isActive() { return active; }
        public void deactivate() {
            this.active = false;
        }
    }
