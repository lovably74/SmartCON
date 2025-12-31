import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function ProfileWorker() {
  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        <h1 className="text-2xl font-bold">내 정보</h1>
        <Card>
          <CardHeader>
            <CardTitle>프로필 정보</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center py-12 text-muted-foreground">
              프로필 정보가 여기에 표시됩니다.
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}



