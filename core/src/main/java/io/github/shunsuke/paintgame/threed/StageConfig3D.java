package io.github.shunsuke.paintgame.threed;

/**
 * Small immutable stage definition for the beginner-friendly 3D prototype.
 * This keeps floor size, obstacle layout, and spawn points in one easy-to-edit place.
 */
public class StageConfig3D {
    private final StageType3D stageType;
    private final int columns;
    private final int rows;
    private final float playerSpawnX;
    private final float playerSpawnZ;
    private final float enemySpawnX;
    private final float enemySpawnZ;
    private final ObstacleSpec[] obstacleSpecs;

    public StageConfig3D(
        StageType3D stageType,
        int columns,
        int rows,
        float playerSpawnX,
        float playerSpawnZ,
        float enemySpawnX,
        float enemySpawnZ,
        ObstacleSpec[] obstacleSpecs
    ) {
        this.stageType = stageType;
        this.columns = columns;
        this.rows = rows;
        this.playerSpawnX = playerSpawnX;
        this.playerSpawnZ = playerSpawnZ;
        this.enemySpawnX = enemySpawnX;
        this.enemySpawnZ = enemySpawnZ;
        this.obstacleSpecs = obstacleSpecs.clone();
    }

    public StageType3D getStageType() {
        return stageType;
    }

    public String getDisplayName() {
        return stageType.getDisplayName();
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public float getPlayerSpawnX() {
        return playerSpawnX;
    }

    public float getPlayerSpawnZ() {
        return playerSpawnZ;
    }

    public float getEnemySpawnX() {
        return enemySpawnX;
    }

    public float getEnemySpawnZ() {
        return enemySpawnZ;
    }

    public ObstacleSpec[] getObstacleSpecs() {
        return obstacleSpecs.clone();
    }

    public static StageConfig3D forType(StageType3D stageType) {
        if (stageType == StageType3D.WIDE_ARENA) {
            return wideArena();
        }
        return trainingStage();
    }

    private static StageConfig3D trainingStage() {
        return new StageConfig3D(
            StageType3D.TRAINING_STAGE,
            12,
            12,
            -4.2f,
            -4.2f,
            4.2f,
            4.2f,
            new ObstacleSpec[] {
                new ObstacleSpec(0f, 0f, 1.65f, 1.65f, 1.35f, false),
                new ObstacleSpec(-2.9f, 0f, 1.45f, 2.7f, 0.82f, true),
                new ObstacleSpec(2.9f, 0f, 1.45f, 2.7f, 0.82f, true),
                new ObstacleSpec(0f, 3.1f, 2.6f, 1.2f, 1.75f, false),
                new ObstacleSpec(0f, -3.1f, 2.6f, 1.2f, 1.75f, false)
            }
        );
    }

    private static StageConfig3D wideArena() {
        return new StageConfig3D(
            StageType3D.WIDE_ARENA,
            16,
            16,
            -5.9f,
            -5.7f,
            5.9f,
            5.7f,
            new ObstacleSpec[] {
                // Central cover block.
                new ObstacleSpec(0f, 0f, 2.4f, 2.0f, 1.45f, false),

                // Side jump platforms.
                new ObstacleSpec(-5.1f, 0f, 1.8f, 3.2f, 0.82f, true),
                new ObstacleSpec(5.1f, 0f, 1.8f, 3.2f, 0.82f, true),

                // Paint-and-climb walls.
                new ObstacleSpec(0f, 5.2f, 4.2f, 1.25f, 1.85f, false),
                new ObstacleSpec(0f, -5.2f, 4.2f, 1.25f, 1.85f, false),

                // Extra side blockers to break long straight shots without choking the map.
                new ObstacleSpec(-1.9f, 2.2f, 1.1f, 1.1f, 1.15f, false),
                new ObstacleSpec(1.9f, -2.2f, 1.1f, 1.1f, 1.15f, false)
            }
        );
    }

    public static class ObstacleSpec {
        private final float centerX;
        private final float centerZ;
        private final float width;
        private final float depth;
        private final float height;
        private final boolean standable;

        public ObstacleSpec(float centerX, float centerZ, float width, float depth, float height, boolean standable) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.width = width;
            this.depth = depth;
            this.height = height;
            this.standable = standable;
        }

        public float getCenterX() {
            return centerX;
        }

        public float getCenterZ() {
            return centerZ;
        }

        public float getWidth() {
            return width;
        }

        public float getDepth() {
            return depth;
        }

        public float getHeight() {
            return height;
        }

        public boolean isStandable() {
            return standable;
        }
    }
}
