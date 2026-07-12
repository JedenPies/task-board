export interface TaskBoardDto {
  id: string;
  name: string;
  is_public: boolean;
  tasks: TaskDto[];
}

export interface TaskDto {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
}

export interface UpdateTaskCommandDto {
  newTitle: string;
  newDescription: string;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
