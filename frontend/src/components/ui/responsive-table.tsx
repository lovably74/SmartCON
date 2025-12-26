import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

interface Column<T> {
  header: string;
  accessorKey?: keyof T;
  cell?: (item: T) => React.ReactNode;
  className?: string;
}

interface ResponsiveTableProps<T> {
  data: T[];
  columns: Column<T>[];
  keyExtractor: (item: T) => string | number;
  onRowClick?: (item: T) => void;
}

export function ResponsiveTable<T>({
  data,
  columns,
  keyExtractor,
  onRowClick,
}: ResponsiveTableProps<T>) {
  return (
    <>
      {/* Desktop View: Table */}
      <div className="hidden md:block rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((col, index) => (
                <TableHead key={index} className={col.className}>
                  {col.header}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.map((item) => (
              <TableRow
                key={keyExtractor(item)}
                onClick={() => onRowClick?.(item)}
                className={cn(onRowClick && "cursor-pointer hover:bg-muted/50")}
              >
                {columns.map((col, index) => (
                  <TableCell key={index} className={col.className}>
                    {col.cell
                      ? col.cell(item)
                      : col.accessorKey
                      ? (item[col.accessorKey] as React.ReactNode)
                      : null}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Mobile View: Card List */}
      <div className="md:hidden space-y-4">
        {data.map((item) => (
          <Card
            key={keyExtractor(item)}
            onClick={() => onRowClick?.(item)}
            className={cn(onRowClick && "cursor-pointer active:bg-muted/50")}
          >
            <CardContent className="p-4 space-y-3">
              {columns.map((col, index) => (
                <div key={index} className="flex justify-between items-center">
                  <span className="text-sm font-medium text-muted-foreground">
                    {col.header}
                  </span>
                  <span className="text-sm">
                    {col.cell
                      ? col.cell(item)
                      : col.accessorKey
                      ? (item[col.accessorKey] as React.ReactNode)
                      : null}
                  </span>
                </div>
              ))}
            </CardContent>
          </Card>
        ))}
      </div>
    </>
  );
}

