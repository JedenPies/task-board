export interface TaskBoardDto {
  id: string;
  name: string;
  tasks: TaskDto[];
}

export interface TaskDto {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
