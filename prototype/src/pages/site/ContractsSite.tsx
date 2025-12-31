import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function ContractsSite() {
  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <h1 className="text-2xl font-bold">계약 관리</h1>
        <Card>
          <CardHeader>
            <CardTitle>계약서 현황</CardTitle>
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



