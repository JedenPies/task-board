export interface TaskBoardDto {
  id: string;
  name: string;
  isPublic: boolean;
  tasks: TaskDto[];
  canChangeVisibility: boolean;
  canEdit: boolean;
}

export interface TaskBoardOverviewDto {
  "id": string,
  "name": string
  "canChangeVisibility": boolean,
  "canEdit": boolean,
  "isPublic": boolean,
}

export interface TaskDto {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  position: number;
}

export interface NewTaskDto {
  title: string;
  description: string;
  status: TaskStatus;
}

export interface UpdateTaskCommandDto {
  newTitle: string;
  newDescription: string;
}

export interface AuthorizationResultDto {
  accessToken: string;
  userDisplayName: string;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
