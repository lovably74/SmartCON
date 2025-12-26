import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function ContractsWorker() {
  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        <h1 className="text-2xl font-bold">전자 계약</h1>
        <Card>
          <CardHeader>
            <CardTitle>계약서 목록</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center py-12 text-muted-foreground">
              계약서 목록이 여기에 표시됩니다.
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}

