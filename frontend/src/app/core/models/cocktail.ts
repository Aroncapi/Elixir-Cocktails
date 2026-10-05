export interface Cocktail {
  id: number;
  name: string;
  category: string;
  ingredients: string;
  imageUrl: string | null;
  active: boolean;
  eventTypes: EventTypeRef[];
  createdAt: string;
}

export interface EventTypeRef {
  id: number;
  code: string;
  name: string;
}
